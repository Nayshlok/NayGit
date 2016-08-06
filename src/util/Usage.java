package util;

public class Usage {

	private static final int MAX_DIE_COUNT = 50;
	private static int dieCount = 0;
	
	
	private static boolean dieIsRecursing(){
		return dieCount++ > MAX_DIE_COUNT;
	}
	
	private static void dieBuiltin(String err, Object... params){
		System.err.print("Fatal: ");
		System.err.printf(err, params);
	}
	
	public static void die(String... err){
		if(dieIsRecursing()){
			System.err.println("fatal: recusion dtected in die handler");
			System.exit(128);
		}
		
		dieBuiltin(err[0], err);
		dieCount = 0;
	}
}
