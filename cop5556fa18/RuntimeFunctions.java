// Yichen Liang    p6   Nov 23   11:59pm
package cop5556fa18;

public class RuntimeFunctions {
	public static final String className = "cop5556fa18/RuntimeFunctions";
	
	public static String sinSig = "(F)F";
	public static float sin(float arg0) {
		return (float) Math.sin((double) arg0);
	}
	
	public static String cosSig = "(F)F";
	public static float cos(float arg0) {
		return (float) Math.cos((double) arg0);
	}
	
	public static String atanSig = "(F)F";
	public static float atan(float arg0) {
		return (float) Math.atan((double) arg0);
	}
	
	public static String absIntSig = "(I)I";
	public static int absInt(int arg0) {
		return Math.abs(arg0);
	}
	
	public static String absSig = "(F)F";
	public static float abs(float arg0) {
		return Math.abs(arg0);
	}
	
	public static String logSig = "(F)F";
	public static float log(float arg0) {
		return (float) Math.log((double) arg0);
	}
}
