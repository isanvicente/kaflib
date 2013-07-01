package ixa.kaflib;

import java.util.List;
import java.util.ArrayList;

/** Chunks are noun, verb or prepositional phrases, spanning terms. */
public class Chunk {

    /** Reference to the main annotationContainer of the document to which this dependency is related (required) */
    private AnnotationContainer annotationContainer;

    /** Chunk's ID (required) */
    private String cid;

    /** Type of the phrase (required) */
    private String phrase;

    /** Declension case (optional) */
    private String chunkcase;

    /** Chunk's target terms (at least one required) */
    private Span<Term> span;

    Chunk(AnnotationContainer annotationContainer, String cid, String phrase, Span<Term> span) {
	if (span.size() < 1) {
	    throw new IllegalStateException("Chunks must contain at least one term target");
	}
	this.annotationContainer = annotationContainer;
	this.cid = cid;
	this.phrase = phrase;
	this.span = span;
    }

    public String getId() {
	return cid;
    }

    public boolean hasHead() {
	return (span.getHead() != null);
    }

    public Term getHead() {
	return span.getHead();
    }

    public String getPhrase() {
	return phrase;
    }

    public void setPhrase(String phrase) {
	this.phrase = phrase;
    }

    public boolean hasCase() {
	return chunkcase != null;
    }

    public String getCase() {
	return chunkcase;
    }

    public void setCase(String chunkcase) {
	this.chunkcase = chunkcase;
    }

    public List<Term> getTerms() {
	return this.span.getTargets();
    }

    public void addTerm(Term term) {
	this.span.addTarget(term);
    }

    public void addTerm(Term term, boolean isHead) {
	this.span.addTarget(term, isHead);
    }

    public Span<Term> getSpan() {
	return this.span;
    }

    public void setSpan(Span<Term> span) {
	this.span = span;
    }

    public String getStr() {
	String str = "";
	for (Term term : this.span.getTargets()) {
	    if (!str.isEmpty()) {
		str += " ";
	    }
	    str += term.getStr();
	}
	return str;
    }
}
