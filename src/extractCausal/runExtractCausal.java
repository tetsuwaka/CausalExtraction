package extractCausal;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import utilities.FileUtilities;

public class runExtractCausal implements Callable<ArrayList<Causal>> {
	private String fileName;
	static boolean flag = false;
	private String[] demonList = FileUtilities.readLines("src/extractCausal/demonstrative_list.txt");
	private ArrayList<String[]> clueList = FileUtilities.readClueList();

	public runExtractCausal(String fileName) {
		super();
		this.fileName = fileName;
	}
	
	@Override
	public ArrayList<Causal> call() throws Exception {
		CausalExtraction ce = new CausalExtraction(this.clueList, this.demonList);
		return ce.getInga(this.fileName);
	}
	
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
	
	public static void main(String[] args) {
		String[] files = FileUtilities.readLines("src/extractCausal/test_list.txt");
		ExecutorService ex = Executors.newFixedThreadPool(2);
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
