package sara;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SARAIO {
	public static final String LOCALAPPDATA;
	
	static {
		LOCALAPPDATA = System.getenv("LOCALAPPDATA")+"\\SARA";
	}
	
	public static class FileExtensionFilter implements FileFilter {
		private final String[] exts;
		
		public FileExtensionFilter(String[] exts) {
			for(int i = 0; i < exts.length; i++) {
				exts[i] = exts[i].trim().toLowerCase();
			}
			this.exts = exts;
		}
		public FileExtensionFilter(String ext) {
			this(new String[] {ext});
		}

		@Override
		public boolean accept(File pathname) {
			String ext = pathname.getName().substring(pathname.getName().lastIndexOf('.')+1);
			for(String e : exts) {
				if(e.equals(ext)) return true;
			}
			return false;
		}
		
	}
	
	public static byte[] loadFile(File f) throws IOException {
		FileInputStream reader = new FileInputStream(f);
		byte[] r = reader.readAllBytes();
		reader.close();
		return r;
	}
	
	public static final int UTF_8 = 0, UTF_8_BOM = 3, UTF_16_BE = 2, UTF_32_BE = 4;
	
	public static int BOM(byte[] f) throws IOException {
		if(f.length < 2) return 0;
		int comp = (f[0] & 0xff) << 8 | (f[1] & 0xff);
//		System.out.println(Integer.toHexString(comp));
		
		switch(comp) {
			case 0xfeff: return UTF_16_BE;
			case 0xfffe: throw new IOException("LE charsets not supported! Your file is either using UTF-16-LE-BOM or UTF-32-LE-BOM (little-endian) encoding!");
		}
		if(f.length == 2) return 0;
		
		comp = comp << 8 | (f[2] & 0xff);
//		System.out.println(Integer.toHexString(comp));
		if(comp == 0xefbbbf) return UTF_8_BOM;
		if(f.length == 3) return 0;
		
		comp = comp << 8 | (f[3] & 0xff);
//		System.out.println(Integer.toHexString(comp));
		if(comp == 0x0000feff) return UTF_32_BE;
		
		return 0;
	}
	
	public static int charsz(int encoding) {
		return encoding % 2 == 0 && encoding > 0 ? encoding : 1;
	}
	
	public static int[] asText(byte[] f) throws IOException {
		final int enc = BOM(f), wt = charsz(enc);
		if((f.length-enc) % wt != 0) System.err.println("End of text field is jagged! Ignoring final "+((f.length-enc) % wt)+" bytes...");
		
		int[] r = new int[(f.length-enc)/wt];
		
		if(wt == 1) for(int i = enc, j = 0; i < f.length; i++, j++)
			r[j] = f[i] & 0xff;
		else if(wt == 2) for(int i = enc+wt-1, j = 0; i < f.length; i+=wt, j++)
			r[j] = ((f[i-1] & 0xff) << 8) | (f[i] & 0xff);
		else if(wt == 4) for(int i = enc+wt-1, j = 0; i < f.length; i+=wt, j++)
			r[j] = ((f[i-3] & 0xff) << 24) | ((f[i-2] & 0xff) << 16) | ((f[i-1] & 0xff) << 8) | (f[i] & 0xff);
		return r;
	}
	
	public static char[] getChars(int[] f, int offset, int len) {
		char[] r = new char[len];
		for(int i = 0; i < len; i++, offset++) {
			r[i] = (char) f[offset];
		}
		return r;
	}
	
	public static char[] getChars(int[] f) {
		return getChars(f, 0, f.length);
	}
	
	public static byte[] toByteStream(char[] in, final int encoding) {
		return toByteStream(in, encoding, 0);
	}
	
	public static byte[] toByteStream(char[] in, final int encoding, final int min_len) {
		final int wt = charsz(encoding);
		byte[] r = new byte[Math.max(min_len, in.length*wt + encoding)];
		
		switch(encoding) {
			case 3: { // UTF8BOM
				r[0] = (byte) 0xef;
				r[1] = (byte) 0xbb;
				r[2] = (byte) 0xbf;
			}
			case 0: {
				for(int i = encoding, j = 0; j < in.length; j++) {
					r[i++] = (byte) (in[j] & 0xff);
				}
				break;
			}
			case 2: { // UTF16
				r[0] = (byte) 0xfe;
				r[1] = (byte) 0xff;
				
				for(int i = encoding, j = 0; j < in.length; j++) {
					r[i++] = (byte) ((in[j] >> 8) & 0xff);
					r[i++] = (byte) ((in[j]) & 0xff);
				}
				break;
			}
			case 4: { // UTF32
				r[0] = (byte) 0x00;
				r[1] = (byte) 0x00;
				r[2] = (byte) 0xfe;
				r[3] = (byte) 0xff;
				
				for(int i = encoding, j = 0; j < in.length; j++) {
					r[i++] = (byte) 0x00;
					r[i++] = (byte) 0x00;
					r[i++] = (byte) ((in[j] >> 8) & 0xff);
					r[i++] = (byte) ((in[j]) & 0xff);
				}
				break;
			}
			default: throw new IllegalArgumentException("Unknown encoding of id "+encoding);
		}
		
		return r;
	}
	
	public static void saveToFile(byte[] field, File f) throws IOException {
		File tmp = getTempFile(f);
		FileOutputStream writer = new FileOutputStream(tmp);
		writer.write(field);
		writer.close();
		finalizeTempFile(tmp, f);
	}
	
	public static File getTempFile(File as) throws IOException {
		File tmp = new File(LOCALAPPDATA+as.getName()+".tmp");
		tmp.createNewFile();
		return tmp;
	}
	
	public static boolean finalizeTempFile(File tmp, File to) {
		if(to.exists()) to.delete();
		to.getParentFile().mkdirs();
		boolean r = tmp.renameTo(to);
		tmp.delete();
		return r;
	}
}
