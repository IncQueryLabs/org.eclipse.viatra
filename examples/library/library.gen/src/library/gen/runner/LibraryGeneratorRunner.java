package library.gen.runner;

import static org.junit.Assert.*;

import java.io.IOException;

import library.gen.LibraryGenerator;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.junit.Test;

public class LibraryGeneratorRunner {

	private static final String path = 
			"/Users/istvanrath/Git/org.eclipse.incquery.examples/library/library.gen/model/generatedLibrary_Istvan.xmi";

	@Test
	public void testGenerateLibrary() throws IOException {
		ResourceSet resourceSet = new ResourceSetImpl();
		URI uri = URI.createFileURI(path);
		Resource resource = resourceSet.createResource(uri);
		try {
		new LibraryGenerator(resource).generateLibrary(
				40 /*numBooks*/, 
				30 /*numAuthors*/, 
				10 /*numCitations*/, 
				50 /*numAuthorships*/);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		resource.save(null);
	}

}
