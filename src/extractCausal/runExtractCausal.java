package extractCausal;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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
					System.err.println("Error: Thread number must be a valid integer");
					help.printHelp("extractCausal [options] [file]", opts);
					System.exit(1);
				}
				if (threadNum <= 0) {
					System.err.println("Error: Thread number must be positive");
					help.printHelp("extractCausal [options] [file]", opts);
					System.exit(1);
				}
			}
			if (cl.hasOption("h")) {
				help.printHelp("extractCausal [options] [file]", opts);
				System.exit(0);
			}
			if (cl.hasOption("s")) {
				String svmFile = cl.getOptionValue("s");
				if (svmFile == null || svmFile.trim().isEmpty()) {
					System.err.println("Error: SVM file path cannot be empty");
					help.printHelp("extractCausal [options] [file]", opts);
					System.exit(1);
				}
				CausalExtraction.setSvmHash(FileUtilities.readSvmResults(svmFile));
				CausalExtraction.svmFlag = true;
			}
			if (cl.hasOption("p")) {
				patternFilePath = cl.getOptionValue("p");
				if (patternFilePath == null || patternFilePath.trim().isEmpty()) {
					System.err.println("Error: Pattern file path cannot be empty");
					help.printHelp("extractCausal [options] [file]", opts);
					System.exit(1);
				}
				patternFlag = true;
			}
			
			if (cl.getArgs().length != 1) {
				System.err.println("Error: Exactly one input file must be specified");
				help.printHelp("extractCausal [options] [file]", opts);
				System.exit(1);
			}
			filePath = cl.getArgs()[0];
			if (filePath == null || filePath.trim().isEmpty()) {
				System.err.println("Error: Input file path cannot be empty");
				help.printHelp("extractCausal [options] [file]", opts);
				System.exit(1);
			}
		} catch (ParseException e1) {
			System.err.println("Error parsing command line arguments: " + e1.getMessage());
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
