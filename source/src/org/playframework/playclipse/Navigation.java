package org.playframework.playclipse;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;
import org.playframework.playclipse.builder.PlayNature;

public final class Navigation {
	private EditorHelper editorHelper;
	private IWorkbenchWindow window;

	public Navigation(EditorHelper editorHelper) {
		this.editorHelper = editorHelper;
		this.window = this.editorHelper.getWindow();
	}

	public void goToAction(String controller, String method) {
		String path = "app/controllers/" + controller + ".java";
		IFile file = this.editorHelper.getProject().getFile(path);
		if (file.exists()) {
			IEditorPart newEditorPart;
			try {
				newEditorPart = FilesAccess.openFile(file, this.window);
				EditorHelper newEditor = new EditorHelper((ITextEditor)newEditorPart);
				int lineNo = -1;
				int i = 0;
				int length = newEditor.lineCount();
				String line;
				IDocument doc = newEditor.getDocument();
				while (i < length && lineNo < 0) {
					line = doc.get(doc.getLineOffset(i), doc.getLineLength(i));
					if (line.contains("public") &&
						line.contains("static") &&
						line.contains("void") &&
						line.contains(method))
					{
						lineNo = i;
					}
					i++;
				}
				FilesAccess.goToLine(newEditorPart, i);
			} catch (CoreException e) {
				// Should never happen
				e.printStackTrace();
			} catch (org.eclipse.jface.text.BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			if (MessageDialog.openConfirm(
					window.getShell(),
					"Playclipse",
					"The file " + path + " can't be found, do you want to create it?")) {
				String content = CodeTemplates.controller(controller);
				FilesAccess.createAndOpen(file, content, FilesAccess.FileType.JAVA);
			}
		}
	}

	public void goToView(String viewName) {
		System.out.println("goToView");
		IProject project = editorHelper.getProject();
		try {
			PlayNature nature = (PlayNature)project.getNature("org.playframework.playclipse.playNature");
			System.out.println("Nature: " + nature);
			System.out.println(nature.getModules());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		IFile file = project.getFile("app/views/" + viewName);
		if (!file.exists()) {
			// The file doesn't exist from the absolute path, let's try from the relative path
			IContainer cont = editorHelper.getFirstParentFor("views");
			file = project.getFile(cont.getProjectRelativePath() + "/" + viewName);
		}
		openOrCreate(file);
	}

	public void openOrCreate(String path) {
		IFile file = editorHelper.getProject().getFile(path);
		openOrCreate(file);
	}

	private void openOrCreate(IFile file) {
		if (file.exists()) {
			try {
				FilesAccess.openFile(file, window);
			} catch (CoreException e) {
				// Should never happen (we checked for file.exist())
				e.printStackTrace();
			}
			return;
		}
		String path = file.getFullPath().toString();
		if (MessageDialog.openConfirm(
				window.getShell(),
				"Playclipse",
				"The file " + path + " can't be found, do you want to create it?")) {
			String[] titleArr = path.split("/");
			String title = titleArr[titleArr.length - 1].replace(".html", "");
			String content = CodeTemplates.view(title);
			FilesAccess.createAndOpen(file, content, FilesAccess.FileType.HTML);
		}
	}

}
