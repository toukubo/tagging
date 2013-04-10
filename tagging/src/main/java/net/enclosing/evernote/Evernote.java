package net.enclosing.evernote;

import java.util.List;

import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.userstore.Constants;
import com.evernote.edam.userstore.UserStore;
import com.evernote.thrift.protocol.TBinaryProtocol;
import com.evernote.thrift.transport.THttpClient;

public class Evernote {

	public static final String authToken = "S=s1:U=5e207:E=144537db68f:C=13cfbcc8a8f:P=1cd:A=en-devtoken:H=ba303372324ce12472ef8e6dbb270a13";
	public static final String evernoteHost = "sandbox.evernote.com";
	public static final String userStoreUrl = "https://" + evernoteHost + "/edam/user";

	// In a real application, you would change the User Agent to a string that describes 
	// your application, using the form company name/app name and version. Using a unique 
	// user agent string helps us provide you with better support. 
	public static final String userAgent = "Evernote/EDAMDemo (Java) " + 
			Constants.EDAM_VERSION_MAJOR + "." + 
			Constants.EDAM_VERSION_MINOR;

	private NoteStore.Client noteStore;
	private String newNoteGuid;
	public Evernote(){

		try {
			// Set up the UserStore client and check that we can speak to the server
			THttpClient userStoreTrans = new THttpClient(userStoreUrl);
			userStoreTrans.setCustomHeader("User-Agent", userAgent);
			TBinaryProtocol userStoreProt = new TBinaryProtocol(userStoreTrans);
			UserStore.Client userStore = new UserStore.Client(userStoreProt, userStoreProt);

			boolean versionOk = userStore.checkVersion("Evernote EDAMDemo (Java)",
					com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR,
					com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
			if (!versionOk) {
				System.err.println("Incomatible Evernote client protocol version");
				return;
			}

			// Get the URL used to interact with the contents of the user's account
			// When your application authenticates using OAuth, the NoteStore URL will
			// be returned along with the auth token in the final OAuth request.
			// In that case, you don't need to make this call.
			String notestoreUrl = userStore.getNoteStoreUrl(authToken);

			// Set up the NoteStore client 
			THttpClient noteStoreTrans = new THttpClient(notestoreUrl);
			noteStoreTrans.setCustomHeader("User-Agent", userAgent);
			TBinaryProtocol noteStoreProt = new TBinaryProtocol(noteStoreTrans);
			noteStore = new NoteStore.Client(noteStoreProt, noteStoreProt);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Evernote evernote = new Evernote();
		try {
			evernote.listNotes();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private void listNotes() 
			throws Exception 
			{    
		// List the notes in the user's account
		System.out.println("Listing notes:");

		// First, get a list of all notebooks
		List<Notebook> notebooks = noteStore.listNotebooks(authToken);

		for (Notebook notebook : notebooks) {
			System.out.println("Notebook: " + notebook.getName());

			// Next, search for the first 100 notes in this notebook, ordering by creation date
			NoteFilter filter = new NoteFilter();
			filter.setNotebookGuid(notebook.getGuid());
			filter.setOrder(NoteSortOrder.CREATED.getValue());
			filter.setAscending(true);

			NoteList noteList = noteStore.findNotes(authToken, filter, 0, 100);
			List<Note> notes = noteList.getNotes();
			for (Note note : notes) {
				System.out.println(" * " + note.getTitle());
			}
		}
		System.out.println();
			}



}
