package sara.localization;

public class EnglishLocalization extends Localization {
	public String toPlural(String str, long count) {
		if(count == 1) return str;
		if(str.endsWith("s")) return str+"es";
		return str+"s";
	}

	@Override
	public int affirmative(String str) {
		switch(str.toLowerCase()) {
			case "on":
			case "yes":
			case "y":
			case "true": return 1;
			case "off":
			case "no":
			case "n":
			case "false": return -1;
			default: return 0;
		}
	}
}
