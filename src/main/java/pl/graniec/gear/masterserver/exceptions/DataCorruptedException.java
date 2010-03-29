package pl.graniec.gear.masterserver.exceptions;

public class DataCorruptedException extends Exception {

	private static final long serialVersionUID = -5399862415268243509L;

	public DataCorruptedException() {
	}

	public DataCorruptedException(String arg0) {
		super(arg0);
	}

	public DataCorruptedException(Throwable arg0) {
		super(arg0);
	}

	public DataCorruptedException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
