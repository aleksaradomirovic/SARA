package sara.localization;

public abstract class Localization {
	/**
	 * @param str Object noun to be made plural
	 * @param count Amount of objects
	 * @return plural form
	 */
	public abstract String toPlural(String str, long count);
	
	/**
	 * Checks if a string is affirmative (like "yes") or negative (like "no")
	 * @return 1 if affirmative, -1 if negative, 0 if neither
	 */
	public abstract int affirmative(String str);
}
