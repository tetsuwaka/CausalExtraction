package extractCausal;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import utilities.FileUtilities;

public class runExtractCausal implements Callable<ArrayList<Causal>> {
	private String fileName;
	private static int threadNum = 2;
	private static boolean patternFlag = false;
	private static String patternFilePath;
	private static String filePath = null;

	public runExtractCausal(String fileName) {
		super();
		this.fileName = fileName;
	}
	
	@Override
	public ArrayList<Causal> call() throws Exception {
		CausalExtraction ce = new CausalExtraction();
		return ce.getInga(this.fileName);
	}

	/**
	 * コマンドライン引数を処理して、変数をセットする
	 * @param args コマンドライン引数
	 */
	private static void setArgs(String[] args) {
		Options opts = new Options();
		opts.addOption("t", "threadNum", true, "Thread Number");
		opts.addOption("p", "pattern", true, "use Prefix Patterns");
		opts.addOption("s", "svm", true, "use SVM results");
		opts.addOption("h", "help", false, "show help");
		org.apache.commons.cli.BasicParser parser = new org.apache.commons.cli.BasicParser();
		org.apache.commons.cli.CommandLine cl;
		HelpFormatter help = new HelpFormatter();

		try {
			cl = parser.parse(opts, args);
			if (cl.hasOption("t")) {
				try {
					threadNum = Integer.parseInt(cl.getOptionValue("t"));
				} catch (NumberFormatException e) {
					help.printHelp("extractCausal [options] [file]", opts);
					System.exit(1);
				}
				if (threadNum <= 0) {
					help.printHelp("extractCausal [options] [file]", opts);
					System.exit(1);
				}
			}
			if (cl.hasOption("h")) {
				help.printHelp("extractCausal [options] [file]", opts);
				System.exit(1);
			}
			if (cl.hasOption("s")) {
				CausalExtraction.setSvmHash(FileUtilities.readSvmResults(cl.getOptionValue("s")));
				CausalExtraction.svmFlag = true;
			}
			if (cl.hasOption("p")) {
				patternFilePath = cl.getOptionValue("p");
				patternFlag = true;
			}
			
			if (cl.getArgs().length != 1) {
				help.printHelp("extractCausal [options] [file]", opts);
				System.exit(1);
			}
			filePath = cl.getArgs()[0];
		} catch (ParseException e1) {
			help.printHelp("extractCausal [options] [file]", opts);
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		// コマンドライン引数の処理
		setArgs(args);
		
		// 手がかり表現と指示詞リストの読み込み
		CausalExtraction.setDemonList(FileUtilities.readLines("demonstrative_list.txt"));
		CausalExtraction.setClueList(FileUtilities.readClueList("clue_list.txt"));
		
		// PrefixPatternを使うようにしてあれば
		if (patternFlag) {
			CausalExtraction.setAdditionalData(FileUtilities.readAdditionalData(patternFilePath));
		}

		String[] files = FileUtilities.readLines(filePath);
		ExecutorService ex = Executors.newFixedThreadPool(threadNum);
		CompletionService<ArrayList<Causal>> completion = new ExecutorCompletionService<ArrayList<Causal>>(ex);

		// タスクの投入
		for (int i = 0; i < files.length; i++) {
			completion.submit(new runExtractCausal(files[i]));
		}

		// 新たなタスクの受付を停止
		ex.shutdown();

		// 結果の収集
		int completedTasks = 0;
		while (completedTasks < files.length) {
			try {
				Future<ArrayList<Causal>> future = completion.take();
				ArrayList<Causal> causalList = future.get();
				for (Causal causal : causalList) {
					System.out.println(causal.toJson());
				}
				completedTasks++;
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				System.err.println("処理が中断されました");
				break;
			} catch (ExecutionException e) {
				System.err.println("タスク実行中にエラーが発生しました: " + e.getCause());
				completedTasks++;
			}
		}

		// 全タスクの完了を待機
		try {
			if (!ex.awaitTermination(60, TimeUnit.SECONDS)) {
				ex.shutdownNow();
			}
		} catch (InterruptedException e) {
			ex.shutdownNow();
		}
	}
}
