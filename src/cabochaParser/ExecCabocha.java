package cabochaParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CaboCha を常駐プロセスとして実行するクラス。
 *
 * 文ごとにプロセスを起動するのではなく、スレッドごとに1つの cabocha プロセスを
 * 起動したままにして標準入出力で文をやり取りする。
 * プロセスは {@link #shutdown()} または JVM 終了時（シャットダウンフック）にすべて kill される。
 *
 * cabocha の実行ファイルはシステムプロパティ {@code cabocha.path} で指定できる。
 * 例: {@code java -Dcabocha.path=/usr/local/bin/cabocha -jar extractCausal.jar ...}
 */
public class ExecCabocha {

	private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

	private static final String CABOCHA_PATH = System.getProperty("cabocha.path",
			IS_WINDOWS ? "C:\\Program Files (x86)\\CaboCha\\bin\\cabocha.exe" : "cabocha");

	/** スレッドごとの常駐プロセス（標準入出力は共有できないためスレッド単位で持つ） */
	private static final ThreadLocal<CabochaProcess> LOCAL_PROCESS = new ThreadLocal<CabochaProcess>();

	/** 起動したすべてのプロセス（終了時に一括 kill するため） */
	private static final Set<CabochaProcess> ALL_PROCESSES = ConcurrentHashMap.newKeySet();

	static {
		// Ctrl+C や System.exit でも cabocha プロセスを残さない
		Runtime.getRuntime().addShutdownHook(new Thread(ExecCabocha::shutdown, "cabocha-shutdown"));
	}

	/**
	 * 文を cabocha で解析し、-f1 形式の出力を行ごとに返す（末尾の EOS 行を含む）。
	 * @param sentence 解析する文
	 * @return cabocha の出力行
	 */
	public static ArrayList<String> exec(String sentence) throws IOException, InterruptedException {
		CabochaProcess proc = LOCAL_PROCESS.get();
		if (proc == null || !proc.isAlive()) {
			if (proc != null) {
				release(proc);
			}
			proc = new CabochaProcess();
			LOCAL_PROCESS.set(proc);
			ALL_PROCESSES.add(proc);
		}

		try {
			return proc.analyze(sentence);
		} catch (IOException e) {
			// 入出力が壊れたプロセスは破棄し、次回呼び出し時に再起動する
			release(proc);
			throw e;
		}
	}

	/**
	 * cabocha が起動できて解析結果を返すかを確認する。
	 * 見つからない・動かない場合は IOException を投げる。処理開始前に呼ぶこと。
	 */
	public static void checkAvailable() throws IOException {
		CabochaProcess proc;
		try {
			proc = new CabochaProcess();
		} catch (IOException e) {
			throw new IOException("cabocha を起動できません: " + CABOCHA_PATH
					+ " (PATH を確認するか -Dcabocha.path=<cabochaのパス> を指定してください)", e);
		}
		try {
			proc.analyze("テスト");
		} catch (IOException e) {
			throw new IOException("cabocha が正常に動作しません: " + CABOCHA_PATH, e);
		} finally {
			proc.destroy();
		}
	}

	/**
	 * 起動中のすべての cabocha プロセスを終了する。
	 * 全処理完了後に呼ぶこと。JVM 終了時にも自動で呼ばれる。
	 */
	public static void shutdown() {
		for (CabochaProcess proc : ALL_PROCESSES) {
			proc.destroy();
		}
		ALL_PROCESSES.clear();
	}

	private static void release(CabochaProcess proc) {
		proc.destroy();
		ALL_PROCESSES.remove(proc);
		LOCAL_PROCESS.remove();
	}

	/** 1つの cabocha プロセスとその標準入出力 */
	private static class CabochaProcess {
		private final Process process;
		private final BufferedWriter writer;
		private final BufferedReader reader;

		CabochaProcess() throws IOException {
			ProcessBuilder pb = new ProcessBuilder(CABOCHA_PATH, "-f1");
			// 標準エラーは読み取らないので、バッファ詰まりで止まらないように親に流す
			pb.redirectError(ProcessBuilder.Redirect.INHERIT);
			this.process = pb.start();
			this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
			this.reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
		}

		boolean isAlive() {
			return process.isAlive();
		}

		ArrayList<String> analyze(String sentence) throws IOException {
			// 改行が含まれると cabocha が複数文として扱い、出力の対応が崩れるため1行にする
			String oneLine = sentence.replace("\r", " ").replace("\n", " ");
			writer.write(oneLine);
			writer.newLine();
			writer.flush();

			ArrayList<String> results = new ArrayList<String>();
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					throw new IOException("cabocha process terminated unexpectedly");
				}
				results.add(line);
				if (line.equals("EOS")) {
					break;
				}
			}
			return results;
		}

		void destroy() {
			try {
				writer.close();
			} catch (IOException e) {
				// 無視して kill する
			}
			process.destroy();
		}
	}
}
