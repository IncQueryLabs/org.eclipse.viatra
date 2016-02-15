/**
 */
package org.eclipse.incquery.examples.eiqlibrary;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Writer</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.incquery.examples.eiqlibrary.Writer#getBooks <em>Books</em>}</li>
 *   <li>{@link org.eclipse.incquery.examples.eiqlibrary.Writer#getFirstBook <em>First Book</em>}</li>
 *   <li>{@link org.eclipse.incquery.examples.eiqlibrary.Writer#getScifiBooks <em>Scifi Books</em>}</li>
 *   <li>{@link org.eclipse.incquery.examples.eiqlibrary.Writer#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.incquery.examples.eiqlibrary.EIQLibraryPackage#getWriter()
 * @model
 * @generated
 */
public interface Writer extends EObject {
    /**
     * Returns the value of the '<em><b>Books</b></em>' reference list.
     * The list contents are of type {@link org.eclipse.incquery.examples.eiqlibrary.Book}.
     * It is bidirectional and its opposite is '{@link org.eclipse.incquery.examples.eiqlibrary.Book#getAuthors <em>Authors</em>}'.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Books</em>' reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Books</em>' reference list.
     * @see org.eclipse.incquery.examples.eiqlibrary.EIQLibraryPackage#getWriter_Books()
     * @see org.eclipse.incquery.examples.eiqlibrary.Book#getAuthors
     * @model opposite="authors"
     * @generated
     */
    EList<Book> getBooks();

    /**
     * Returns the value of the '<em><b>First Book</b></em>' reference.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>First Book</em>' reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>First Book</em>' reference.
     * @see org.eclipse.incquery.examples.eiqlibrary.EIQLibraryPackage#getWriter_FirstBook()
     * @model transient="true" changeable="false" volatile="true" derived="true"
     *        annotation="http://www.eclipse.org/emf/2002/GenModel get='EList<Book> allBooks = getBooks();\r\nif(!allBooks.isEmpty()){\r\n  return allBooks.get(0);\r\n}\r\nreturn null;'"
     * @generated
     */
    Book getFirstBook();

    /**
     * Returns the value of the '<em><b>Scifi Books</b></em>' reference list.
     * The list contents are of type {@link org.eclipse.incquery.examples.eiqlibrary.Book}.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Scifi Books</em>' reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Scifi Books</em>' reference list.
     * @see org.eclipse.incquery.examples.eiqlibrary.EIQLibraryPackage#getWriter_ScifiBooks()
     * @model transient="true" changeable="false" volatile="true" derived="true"
     *        annotation="http://www.eclipse.org/emf/2002/GenModel get='EList<Book> allBooks = getBooks();\r\njava.util.List<Book> scifiBooks = new java.util.ArrayList<Book>();\r\nfor (Book book : allBooks) {\r\n  if(book.getCategory().contains(org.eclipse.incquery.examples.eiqlibrary.BookCategory.SCI_FI)) {\r\n     scifiBooks.add(book);\r\n  }\r\n}\r\nreturn org.eclipse.emf.common.util.ECollections.asEList(scifiBooks);'"
     * @generated
     */
    EList<Book> getScifiBooks();

    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Name</em>' attribute.
     * @see #setName(String)
     * @see org.eclipse.incquery.examples.eiqlibrary.EIQLibraryPackage#getWriter_Name()
     * @model
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link org.eclipse.incquery.examples.eiqlibrary.Writer#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

} // Writer
