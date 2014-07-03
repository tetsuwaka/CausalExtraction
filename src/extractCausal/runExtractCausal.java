package extractCausal;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import utilities.FileUtilities;

public class runExtractCausal implements Callable<ArrayList<Causal>> {
	private String fileName;
	static boolean flag = false;
	private static int threadNum = 2;
	private static boolean patternFlag = false;
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
	 * JSON形式の出力を行う(同期的に)
	 * @param causalList カボリスト
	 */
	public static synchronized void printJson(ArrayList<Causal> causalList) {
		for (Causal causal : causalList) {
			if (!flag) {
				System.out.print(causal.toJson());
				flag = true;
			} else {
				System.out.println(",");
				System.out.print(causal.toJson());
			}
		}
	}
	
	/**
	 * コマンドライン引数を処理して、変数をセットする
	 * @param args コマンドライン引数
	 */
	private static void setArgs(String[] args) {
		Options opts = new Options();
		opts.addOption("t", "threadNum", true, "Thread Number");
		opts.addOption("p", "pattern", false, "use Prefix Pattern");
		opts.addOption("h", "help", false, "show help");
		BasicParser parser = new BasicParser();
		CommandLine cl;
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
			if (cl.hasOption("p")) {
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
			CausalExtraction.setAdditionalData(FileUtilities.readAdditionalData("additional_data.txt"));
		}

		String[] files = FileUtilities.readLines(filePath);
		ExecutorService ex = Executors.newFixedThreadPool(threadNum);
		CompletionService<ArrayList<Causal>> completion = new ExecutorCompletionService<ArrayList<Causal>>(ex);
		for (int i = 0; i < files.length; i++) {
			completion.submit(new runExtractCausal(files[i]));
		}
		
		ex.shutdown();
		
		System.out.println("[");
		for (int i = 0; i < files.length; i++) {
			try {
				Future<ArrayList<Causal>> future = completion.take();
				ArrayList<Causal> causalList = future.get();
				runExtractCausal.printJson(causalList);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				continue;
			} catch (ExecutionException e) {
				continue;
			}
		}
		System.out.println();
		System.out.println("]");
	}

}
