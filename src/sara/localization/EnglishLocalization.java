package sara.localization;

public class EnglishLocalization implements Localization {
	@Override
	public int affirmative(String str) {
		switch(str.toLowerCase().trim()) {
			case "yes":
			case "y":
			case "true":
			case "on": return 1;
			case "no":
			case "n":
			case "false":
			case "off": return -1;
			default: return 0;
		}
	}
}
