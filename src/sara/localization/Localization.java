package sara.localization;

public interface Localization {
	/**
	 * 
	 * @param str String to check if an affirmative input.
	 * @return n > 0 if affirmative, n < 0 if negative, n = 0 if unknown
	 */
	public int affirmative(String str);
}
