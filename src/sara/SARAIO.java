package sara;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SARAIO {
	public static final long file_max_length = Integer.MAX_VALUE;
	public static final int bufsz_byte = 16384,
							bufsz_char = bufsz_byte / 2;
	
	public static final File LOCALAPPDATA;
	
	static {
		LOCALAPPDATA = new File(System.getenv("LOCALAPPDATA")+File.separator+"SARA");
		LOCALAPPDATA.mkdirs();
	}
	
	public static byte[] loadFile(File f) throws IOException {
		f = makeAbsolute(f);
		if(f.length() > Integer.MAX_VALUE) throw new FileTooLargeException(f);
		int filesz = (int) f.length();
		FileInputStream reader = new FileInputStream(f);
		byte[] out = new byte[filesz],
			buf = new byte[bufsz_byte];
		int len, pos = 0;
		while((len = reader.read(buf)) > 0) {
			copyIntoByteField(out, buf, pos);
			pos += len;
		}
		reader.close();
		return out;
	}
	
	public static void save(File f, byte[] bytes) throws IOException {
		File temp = makeAbsolute(new File(f.getName()+".tmp"));
		temp.delete();
		temp.createNewFile();
		f = makeAbsolute(f);
		
		FileOutputStream writer = new FileOutputStream(temp, false);
		writer.write(bytes);
		writer.close();
		
		f.delete();
		f.getParentFile().mkdirs();
		temp.renameTo(f);
	}
	
	private static final byte[] BE_BOM = new byte[] { (byte) 0xFE, (byte) 0xFF },
			NL_CHAR = new byte[] { (byte) 0x00, (byte) 0x0A };
	public static void saveText(File f, char[][] field) throws IOException {
		File temp = makeAbsolute(new File(f.getName()+".tmp"));
		temp.delete();
		temp.createNewFile();
		f = makeAbsolute(f);
		
		FileOutputStream writer = new FileOutputStream(temp,true);
		writer.write(BE_BOM);
		for(char[] buf : field) {
			writer.write(toBE(buf));
			writer.write(NL_CHAR);
		}
		writer.close();

		f.delete();
		f.getParentFile().mkdirs();
		temp.renameTo(f);
	}
	
	public static byte[] toBE(char[] field) {
		byte[] out = new byte[field.length*2];
		
		int f;
		for(int i = 0, j = 0; i < field.length; i++, j+=2) {
			f = field[i];
			out[j] = (byte)(f >> 8);
			out[j+1] = (byte)(f);
		}
		
//		System.out.println(out.length);
		return out;
	}
	
	public static char[] loadFileAsText(File f) throws IOException {
		return toCharField(loadFile(f));
	}
		
	public static void printByteField(byte[] field) {
		for(int i = 0; i < field.length; i++) {
			System.out.print((char)(field[i])+" ");
			if(i % 8 == 0) System.out.println();
		}
	}
	
	public static char[] toCharField(byte[] bytes) throws IOException {
		if(bytes.length % 2 != 0 || bytes.length < 2) throw new IOException("Odd number of bytes -- char parser doesn't accept UTF-8 encoding!");
		int BOM0 = toBigEndian(bytes[0], bytes[1]);
//		System.out.println(BOM0);
		
		if(BOM0 == 0xFFFE) return charFieldLE(bytes, 2); // Little-endian
		else if(BOM0 == 0xFEFF) return charFieldBE(bytes, 2); // Big-endian declared
		else if(BOM0 == 0xEFBB) throw new IOException("Won't accept UTF-8 encoding!"); //UTF-8 BOM
		else return charFieldBE(bytes, 0); // no BOM declared, so Big-endian undeclared 
	}
	
//	private static long toBigEndian(byte b0, byte b1, byte b2, byte b3) {
//		return ((b0 & 0xff) << 24) | ((b1 & 0xff) << 16) | ((b2 & 0xff) << 8) | (b3 & 0xff);
//	}
	
	public static char[] charFieldBE(byte[] bytes, int from) {
		if(bytes.length % 2 != 0) throw new IllegalArgumentException("Odd number of bytes in field!");
		char[] r = new char[(bytes.length-from)/2];
		
		for(int i = from, pos = 0; i < bytes.length; i+=2, pos++) 
			r[pos] = (char)toBigEndian(bytes[i], bytes[i+1]);
		
		return r;
	}
	
	public static char[] charFieldLE(byte[] bytes, int from) {
		if(bytes.length % 2 != 0) throw new IllegalArgumentException("Odd number of bytes in field!");
		char[] r = new char[(bytes.length-from)/2];
		
		for(int i = from, pos = 0; i < bytes.length; i+=2, pos++) 
			r[pos] = (char)toLittleEndian(bytes[i], bytes[i+1]);
		
		return r;
	}
	
	public static int toBigEndian(byte b0, byte b1) {
//		System.out.println(b0+" "+b1);
		return ((b0 & 0xff) << 8) | (b1 & 0xff);
	}
	
	public static int toLittleEndian(byte b0, byte b1) {
		return ((b1 & 0xff) << 8) | (b0 & 0xff);
	}
	
	public static File makeAbsolute(File f) {
		return makeAbsolute(f, LOCALAPPDATA);
	}
	
	public static File makeAbsolute(File f, File dir) {
		if(f.isAbsolute()) return f;
		return new File(dir+File.separator+f);
	}
	
	private static void copyIntoByteField(byte[] target, byte[] from, int offset) {
		for(int i = 0; i < from.length && offset < target.length; i++, offset++) {
			target[offset] = from[i];
		}
	}
	
	public static class FileTooLargeException extends IOException {
		private static final long serialVersionUID = 4534735515174090596L;
		
		public FileTooLargeException(File f) {
			super("File '"+f.getPath()+"' was too large to load! (size: "+f.length()+" bytes)");
		}
	}
}
