package org.eclipse.incquery.runtime.runonce.tests.util;

import org.eclipse.incquery.examples.eiqlibrary.Book;
import org.eclipse.incquery.examples.eiqlibrary.Writer;
import org.eclipse.incquery.runtime.api.IMatchProcessor;
import org.eclipse.incquery.runtime.runonce.tests.BookAuthorsMatch;

/**
 * A match processor tailored for the org.eclipse.incquery.runtime.runonce.tests.bookAuthors pattern.
 * 
 * Clients should derive an (anonymous) class that implements the abstract process().
 * 
 */
@SuppressWarnings("all")
public abstract class BookAuthorsProcessor implements IMatchProcessor<BookAuthorsMatch> {
  /**
   * Defines the action that is to be executed on each match.
   * @param pBook the value of pattern parameter book in the currently processed match
   * @param pAuthor the value of pattern parameter author in the currently processed match
   * 
   */
  public abstract void process(final Book pBook, final Writer pAuthor);
  
  @Override
  public void process(final BookAuthorsMatch match) {
    process(match.getBook(), match.getAuthor());
    
  }
}
