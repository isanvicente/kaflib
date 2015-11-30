package ixa.kaflib;

import ixa.kaflib.Opinion.OpinionExpression;
import ixa.kaflib.Opinion.OpinionHolder;
import ixa.kaflib.Opinion.OpinionTarget;
import ixa.kaflib.Predicate.Role;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.io.File;
import java.io.Reader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jdom2.JDOMException;
import org.jdom2.Element;


@SuppressWarnings("unchecked")
/** Respresents a KAF document. It's the main class of the library, as it keeps all elements of the document (word forms, terms, entities...) and manages all object creations. The document can be created by the user calling it's methods, or loading from an existing XML file.*/
public class KAFDocument implements Serializable {
    
    private static final long serialVersionUID = 42L; // Serializable...
    
    public enum Layer {
	TEXT,
	TERMS,
	ENTITIES,
	CHUNKS,
	DEPS,
	CONSTITUENCY,
	COREFERENCES,
	OPINIONS,
	CAUSAL_RELATIONS,
	TEMPORAL_RELATIONS,
	SRL,
	TIME_EXPRESSIONS,
	FACTUALITIES,
	FACTUALITY_LAYER,
	MARKABLES,
	PROPERTIES,
	CATEGORIES,
	RELATIONS,
	LINKED_ENTITIES,
	TOPICS,
	ATTRIBUTION,
    }

    public enum AnnotationType {
	WF,
	TERM,
	MW,
	COMPONENT,
	SENTIMENT,
	ENTITY,
	CHUNK,
	DEP,
	TREE,
	NON_TERMINAL,
	TERMINAL,
	EDGE,
	COREF,
	OPINION,
	OPINION_HOLDER,
	OPINION_TARGET,
	OPINION_EXPRESSION,
	CLINK,
	TLINK,
	PREDICATE_ANCHOR,
	PREDICATE,
	ROLE,
	TIMEX3,
	FACTUALITY,
	FACTVALUE,
	MARK,
	PROPERTY,
	CATEGORY,
	LINKED_ENTITY,
	RELATION,
	TOPIC,
	STATEMENT,
	STATEMENT_TARGET,
	STATEMENT_SOURCE,
	STATEMENT_CUE,
    }

    static final List<AnnotationType> TOP_TYPES;
    static {
	TOP_TYPES = Arrays.asList(
		AnnotationType.WF,
		AnnotationType.TERM,
		AnnotationType.MW,
		AnnotationType.ENTITY,
		AnnotationType.CHUNK,
		AnnotationType.DEP,
		AnnotationType.TREE,
		AnnotationType.COREF,
		AnnotationType.OPINION,
		AnnotationType.CLINK,
		AnnotationType.TLINK,
		AnnotationType.PREDICATE_ANCHOR,
		AnnotationType.PREDICATE,
		AnnotationType.TIMEX3,
		AnnotationType.FACTUALITY,
		AnnotationType.FACTVALUE,
		AnnotationType.MARK,
		AnnotationType.PROPERTY,
		AnnotationType.CATEGORY,
		AnnotationType.LINKED_ENTITY,
		AnnotationType.RELATION,
		AnnotationType.TOPIC,
		AnnotationType.STATEMENT);
    }

    static final Map<AnnotationType, Layer> TYPE_2_LAYER; // Only top level annotation types
    static {
	TYPE_2_LAYER = new HashMap<AnnotationType, Layer>();
	TYPE_2_LAYER.put(AnnotationType.WF, Layer.TEXT);
	TYPE_2_LAYER.put(AnnotationType.TERM, Layer.TERMS);
	TYPE_2_LAYER.put(AnnotationType.MW, Layer.TERMS);
	TYPE_2_LAYER.put(AnnotationType.ENTITY, Layer.ENTITIES);
	TYPE_2_LAYER.put(AnnotationType.CHUNK, Layer.CHUNKS);
	TYPE_2_LAYER.put(AnnotationType.DEP, Layer.DEPS);
	TYPE_2_LAYER.put(AnnotationType.TREE, Layer.CONSTITUENCY);
	TYPE_2_LAYER.put(AnnotationType.COREF, Layer.COREFERENCES);
	TYPE_2_LAYER.put(AnnotationType.OPINION, Layer.OPINIONS);
	TYPE_2_LAYER.put(AnnotationType.CLINK, Layer.CAUSAL_RELATIONS);
	TYPE_2_LAYER.put(AnnotationType.TLINK, Layer.TEMPORAL_RELATIONS);
	TYPE_2_LAYER.put(AnnotationType.PREDICATE_ANCHOR, Layer.TEMPORAL_RELATIONS);
	TYPE_2_LAYER.put(AnnotationType.PREDICATE, Layer.SRL);
	TYPE_2_LAYER.put(AnnotationType.TIMEX3, Layer.TIME_EXPRESSIONS);
	TYPE_2_LAYER.put(AnnotationType.FACTUALITY, Layer.FACTUALITIES);
	TYPE_2_LAYER.put(AnnotationType.FACTVALUE, Layer.FACTUALITY_LAYER);
	TYPE_2_LAYER.put(AnnotationType.MARK, Layer.MARKABLES);
	TYPE_2_LAYER.put(AnnotationType.PROPERTY, Layer.PROPERTIES);
	TYPE_2_LAYER.put(AnnotationType.CATEGORY, Layer.CATEGORIES);
	TYPE_2_LAYER.put(AnnotationType.LINKED_ENTITY, Layer.LINKED_ENTITIES);
	TYPE_2_LAYER.put(AnnotationType.RELATION, Layer.RELATIONS);
	TYPE_2_LAYER.put(AnnotationType.TOPIC, Layer.TOPICS);
	TYPE_2_LAYER.put(AnnotationType.STATEMENT, Layer.ATTRIBUTION);
    }
    
    static final Map<Layer, List<AnnotationType>> LAYER_2_TYPES; // Only top level annotation types
    static {
	LAYER_2_TYPES = new HashMap<Layer, List<AnnotationType>>();
	for (AnnotationType type : TYPE_2_LAYER.keySet()) {
	    Layer layer = TYPE_2_LAYER.get(type);
	    List<AnnotationType> layerTypes = LAYER_2_TYPES.get(layer);
	    if (layerTypes == null) {
		layerTypes = new ArrayList<AnnotationType>();
		LAYER_2_TYPES.put(layer, layerTypes);
	    }
	    layerTypes.add(type);
	}
    }
    
    static final Map<AnnotationType, Class<?>> TYPE_2_CLASS;
    static {
	TYPE_2_CLASS = new HashMap<AnnotationType, Class<?>>();
	TYPE_2_CLASS.put(AnnotationType.WF, WF.class);
	TYPE_2_CLASS.put(AnnotationType.TERM, Term.class);
	TYPE_2_CLASS.put(AnnotationType.COMPONENT, Term.class);
	TYPE_2_CLASS.put(AnnotationType.MW, Term.class);
	TYPE_2_CLASS.put(AnnotationType.ENTITY, Entity.class);
	TYPE_2_CLASS.put(AnnotationType.CHUNK, Chunk.class);
	TYPE_2_CLASS.put(AnnotationType.DEP, Dep.class);
	TYPE_2_CLASS.put(AnnotationType.TREE, Tree.class);
	TYPE_2_CLASS.put(AnnotationType.NON_TERMINAL, NonTerminal.class);
	TYPE_2_CLASS.put(AnnotationType.TERMINAL, Terminal.class);
	TYPE_2_CLASS.put(AnnotationType.COREF, Coref.class);
	TYPE_2_CLASS.put(AnnotationType.OPINION, Opinion.class);
	TYPE_2_CLASS.put(AnnotationType.OPINION_HOLDER, OpinionHolder.class);
	TYPE_2_CLASS.put(AnnotationType.OPINION_TARGET, OpinionTarget.class);
	TYPE_2_CLASS.put(AnnotationType.OPINION_EXPRESSION, OpinionExpression.class);
	TYPE_2_CLASS.put(AnnotationType.CLINK, CLink.class);
	TYPE_2_CLASS.put(AnnotationType.TLINK, TLink.class);
	TYPE_2_CLASS.put(AnnotationType.PREDICATE, Predicate.class);
	TYPE_2_CLASS.put(AnnotationType.ROLE, Role.class);
	TYPE_2_CLASS.put(AnnotationType.TIMEX3, Timex3.class);
	TYPE_2_CLASS.put(AnnotationType.FACTUALITY, Factuality.class);
	TYPE_2_CLASS.put(AnnotationType.FACTVALUE, Factvalue.class);
	TYPE_2_CLASS.put(AnnotationType.MARK, Mark.class);
	TYPE_2_CLASS.put(AnnotationType.PROPERTY, Feature.class);
	TYPE_2_CLASS.put(AnnotationType.CATEGORY, Feature.class);
	TYPE_2_CLASS.put(AnnotationType.LINKED_ENTITY, LinkedEntity.class);
	TYPE_2_CLASS.put(AnnotationType.RELATION, Relation.class);
	TYPE_2_CLASS.put(AnnotationType.TOPIC, Topic.class);
	TYPE_2_CLASS.put(AnnotationType.STATEMENT, Statement.class);
    }


    /** Language identifier */
    private String lang;

    /** KAF version */
    private String version;

    /** Linguistic processors */
    private Map<String, List<LinguisticProcessor>> lps;

    private FileDesc fileDesc;

    private Public _public;

    /** Keeps all the annotations of the document */
    private AnnotationContainer annotationContainer;

    /** Creates an empty KAFDocument element */
    public KAFDocument(String lang, String version) {
	this.lang = lang;
	this.version = version;
	this.lps = new LinkedHashMap<String, List<LinguisticProcessor>>();
	this.annotationContainer = new AnnotationContainer();
    }

    /** Creates a new KAFDocument and loads the contents of the file passed as argument
     * @param file an existing KAF file to be loaded into the library.
     */
    public static KAFDocument createFromFile(File file) throws IOException {
	KAFDocument kaf = null;
	try {
	    kaf = ReadWriteManager.load(file);
	} catch(JDOMException e) {
	    e.printStackTrace();
	}
	return kaf;
    }

    /** Creates a new KAFDocument loading the content read from the reader given on argument.
     * @param stream Reader to read KAF content.
     */
    public static KAFDocument createFromStream(Reader stream) throws IOException, JDOMException {
	KAFDocument kaf = null;
	kaf = ReadWriteManager.load(stream);
	return kaf;
    }

    /** Sets the language of the processed document */
    public void setLang(String lang) {
	this.lang = lang;
    }

    /** Returns the language of the processed document */
    public String getLang() {
	return lang;
    }

    /** Sets the KAF version */
    public void setVersion(String version) {
	this.version = version;
    }

    /** Returns the KAF version */
    public String getVersion() {
	return version;
    }

    /** Adds a linguistic processor to the document header. The timestamp is added implicitly. */
    public LinguisticProcessor addLinguisticProcessor(String layer, String name) {
	LinguisticProcessor lp = new LinguisticProcessor(name, layer);
	//lp.setBeginTimestamp(timestamp); // no default timestamp
	List<LinguisticProcessor> layerLps = lps.get(layer);
	if (layerLps == null) {
	    layerLps = new ArrayList<LinguisticProcessor>();
	    lps.put(layer, layerLps);
	}
	layerLps.add(lp);
	return lp;
    }

    public void addLinguisticProcessors(Map<String, List<LinguisticProcessor>> lps) {
	for (Map.Entry<String, List<LinguisticProcessor>> entry : lps.entrySet()) {
	    List<LinguisticProcessor> layerLps = entry.getValue();
	    for (LinguisticProcessor lp : layerLps) {
		LinguisticProcessor newLp = this.addLinguisticProcessor(entry.getKey(), lp.name);
		if (lp.hasTimestamp()) newLp.setTimestamp(lp.getTimestamp());
		if (lp.hasBeginTimestamp()) newLp.beginTimestamp = lp.beginTimestamp;
		if (lp.hasEndTimestamp()) newLp.setEndTimestamp(lp.getEndTimestamp());
		if (lp.hasVersion()) newLp.setVersion(lp.getVersion());
	    }
	}
    }

    /** Returns a hash of linguistic processors from the document.
     *  Hash: layer => LP
     */
    public Map<String, List<LinguisticProcessor>> getLinguisticProcessors() {
	return lps;
    }

    public List<LinguisticProcessor> getLinguisticProcessorList() {
	List<LinguisticProcessor> result = new ArrayList<LinguisticProcessor>();
	for (List<LinguisticProcessor> lps : this.lps.values()) {
	    for (LinguisticProcessor lp : lps) {
		result.add(lp);
	    }
	}
	return result;
    }

    /** Returns wether the given linguistic processor is already defined or not. Both name and version must be exactly the same. */
    public boolean linguisticProcessorExists(String layer, String name, String version) {
	List<LinguisticProcessor> layerLPs = lps.get(layer);
	if (layerLPs == null) {
	    return false;
	}
	for (LinguisticProcessor lp : layerLPs) {
	    if (lp.version == null) {
		return false;
	    }
	    else if (lp.name.equals(name) && lp.version.equals(version)) {
		return true;
	    }
	}
	return false;
    }

    /** Returns wether the given linguistic processor is already defined or not. Both name and version must be exactly the same. */
    public boolean linguisticProcessorExists(String layer, String name) {
	List<LinguisticProcessor> layerLPs = lps.get(layer);
	if (layerLPs == null) {
	    return false;
	}
	for (LinguisticProcessor lp : layerLPs) {
	    if (lp.version != null) {
		return false;
	    }
	    else if (lp.name.equals(name)) {
		return true;
	    }
	}
	return false;
    }

    public FileDesc createFileDesc() {
	this.fileDesc = new FileDesc();
	return this.fileDesc;
    }

    public FileDesc getFileDesc() {
	return this.fileDesc;
    }

    public Public createPublic() {
	this._public = new Public();
	return this._public;
    }

    public Public getPublic() {
	return this._public;
    }

    /** Returns the annotation container used by this object */
    AnnotationContainer getAnnotationContainer() {
	return annotationContainer;
    }

    /** Set raw text **/
    public void setRawText(String rawText) {
	annotationContainer.setRawText(rawText);
    }

    public WF newWF(int offset, String form, int sent) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.WF);
	return this.newWF(newId, offset, form, sent);
    }

    public WF newWF(String id, int offset, String form, int sent) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.WF, id);
	WF newWF = new WF(this.annotationContainer, id, offset, form.length(), form, sent);
	annotationContainer.add(newWF, AnnotationType.WF);
	return newWF;
    }

    /** Creates a new Term. It assigns an appropriate ID to it. The Term is added to the document object.
     * @param type the type of the term. There are two types of term: open and close.
     * @param lemma the lemma of the term.
     * @param pos part of speech of the term.
     * @param wfs the list of word forms this term is formed by.
     * @return a new term.
     */
    public Term newTerm(Span<WF> span) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.TERM);
	return this.newTerm(newId, span);
    }

    /** Creates a Term object to load an existing term. It receives the ID as an argument. The Term is added to the document object.
     * @param id term's ID.
     * @param type type of term. There are two types of term: open and close.
     * @param lemma the lemma of the term.
     * @param pos part of speech of the term.
     * @param wfs the list of word forms this term is formed by.
     * @return a new term.
     */
    public Term newTerm(String id, Span<WF> span) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.TERM, id);
	Term newTerm = new Term(this.annotationContainer, id, span);
	annotationContainer.add(newTerm, AnnotationType.TERM);
	return newTerm;
    }

    public Compound newCompound() {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.MW);
	return this.newCompound(newId);
    }

    public Compound newCompound(String id) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.MW, id);
	Compound compound = new Compound(this.annotationContainer, id);
	annotationContainer.add(compound, AnnotationType.MW);
	return compound;
    }

    /** Creates a Sentiment object.
     * @return a new sentiment.
     */
    public Term.Sentiment newSentiment() {
	Term.Sentiment newSentiment = new Term.Sentiment(this.annotationContainer);
	return newSentiment;
    }

    public Mark newMark(Span<WF> span) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.MARK);
	return this.newMark(newId, span);
    }
    
    public Mark newMark(String id, Span<WF> span) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.MARK, id);
	Mark newMark = new Mark(this.annotationContainer, id, span);
	annotationContainer.add(newMark, AnnotationType.MARK);
	return newMark;
    }

    /** Creates a new dependency. The Dep is added to the document object.
     * @param from the origin term of the dependency.
     * @param to the target term of the dependency.
     * @param rfunc relational function of the dependency.
     * @return a new dependency.
     */
    public Dep newDep(Term from, Term to, String rfunc) {
	Dep newDep = new Dep(this.annotationContainer, from, to, rfunc);
	annotationContainer.add(newDep, AnnotationType.DEP);
	return newDep;
    }

    /** Creates a new chunk. It assigns an appropriate ID to it. The Chunk is added to the document object.
     * @param head the chunk head.
     * @param phrase type of the phrase.
     * @param terms the list of the terms in the chunk.
     * @return a new chunk.
     */
    public Chunk newChunk(String phrase, Span<Term> span) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.CHUNK);
	return this.newChunk(newId, phrase, span);
    }

    /** Creates a chunk object to load an existing chunk. It receives it's ID as an argument. The Chunk is added to the document object.
     * @param id chunk's ID.
     * @param head the chunk head.
     * @param phrase type of the phrase.
     * @param terms the list of the terms in the chunk.
     * @return a new chunk.
     */
    public Chunk newChunk(String id, String phrase, Span<Term> span) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.CHUNK, id);
	Chunk newChunk = new Chunk(this.annotationContainer, id, span);
	newChunk.setPhrase(phrase);
	annotationContainer.add(newChunk, AnnotationType.CHUNK);
	return newChunk;
    }

    /** Creates a new Entity. It assigns an appropriate ID to it. The entity is added to the document object.
     * @param type entity type. 8 values are posible: Person, Organization, Location, Date, Time, Money, Percent, Misc.
     * @param references it contains one or more span elements. A span can be used to reference the different occurrences of the same named entity in the document. If the entity is composed by multiple words, multiple target elements are used.
     * @return a new named entity.
     */
    public Entity newEntity(List<Span<Term>> references) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.ENTITY);
	return this.newEntity(newId, references);
    }

    /** Creates an Entity object to load an existing entity. It receives the ID as an argument. The entity is added to the document object.
     * @param id the ID of the named entity.
     * @param type entity type. 8 values are posible: Person, Organization, Location, Date, Time, Money, Percent, Misc.
     * @param references it contains one or more span elements. A span can be used to reference the different occurrences of the same named entity in the document. If the entity is composed by multiple words, multiple target elements are used.
     * @return a new named entity.
     */
    public Entity newEntity(String id, List<Span<Term>> references) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.ENTITY, id);
	Entity newEntity = new Entity(this.annotationContainer, id, references);
	annotationContainer.add(newEntity, AnnotationType.ENTITY);
	return newEntity;
    }

    /** Creates a new coreference. It assigns an appropriate ID to it. The Coref is added to the document.
     * @param references different mentions (list of targets) to the same entity.
     * @return a new coreference.
     */
    public Coref newCoref(List<Span<Term>> mentions) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.COREF);
	return this.newCoref(newId, mentions);
    }

    /** Creates a coreference object to load an existing Coref. It receives it's ID as an argument. The Coref is added to the document.
     * @param id the ID of the coreference.
     * @param references different mentions (list of targets) to the same entity.
     * @return a new coreference.
     */
    public Coref newCoref(String id, List<Span<Term>> mentions) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.COREF, id);
	Coref newCoref = new Coref(this.annotationContainer, id, mentions);
	annotationContainer.add(newCoref, AnnotationType.COREF);
	return newCoref;
    }

     /** Creates a new timeExpressions. It assigns an appropriate ID to it. The Coref is added to the document.
     * @param references different mentions (list of targets) to the same entity.
     * @return a new timex3.
     */
    public Timex3 newTimex3(String type) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.TIMEX3);
	return this.newTimex3(newId, type);
    }

    /** Creates a timeExpressions object to load an existing Timex3. It receives it's ID as an argument. The Timex3 is added to the document.
     * @param id the ID of the coreference.
     * @param references different mentions (list of targets) to the same entity.
     * @return a new timex3.
     */
    public Timex3 newTimex3(String id, String type) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.TIMEX3, id);
	Timex3 newTimex3 = new Timex3(this.annotationContainer, id, type);
	annotationContainer.add(newTimex3, AnnotationType.TIMEX3);
	return newTimex3;
    }

    public TLink newTLink(TLinkReferable from, TLinkReferable to, String relType) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.TLINK);
	return this.newTLink(newId, from, to, relType);
    }

    public TLink newTLink(String id, TLinkReferable from, TLinkReferable to, String relType) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.TLINK, id);
	TLink newTLink = new TLink(this.annotationContainer, id, from, to, relType);
	annotationContainer.add(newTLink, AnnotationType.TLINK);
	return newTLink;
    }

    public PredicateAnchor newPredicateAnchor(Span<Predicate> span) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.PREDICATE_ANCHOR);
	return this.newPredicateAnchor(newId, span);
    }
    
    public PredicateAnchor newPredicateAnchor(String id, Span<Predicate> span) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.PREDICATE_ANCHOR, id);
	PredicateAnchor newPredicateAnchor = new PredicateAnchor(this.annotationContainer, id, span);
	annotationContainer.add(newPredicateAnchor, AnnotationType.PREDICATE_ANCHOR);
	return newPredicateAnchor;
    }
    
    public PredicateAnchor newPredicateAnchor(Timex3 anchorTime, Timex3 beginPoint, Timex3 endPoint, Span<Predicate> span) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.PREDICATE_ANCHOR);
	return this.newPredicateAnchor(newId, anchorTime, beginPoint, endPoint, span);
    }
    
    public PredicateAnchor newPredicateAnchor(String id, Timex3 anchorTime, Timex3 beginPoint, Timex3 endPoint, Span<Predicate> span) {
	PredicateAnchor newPa = this.newPredicateAnchor(id, span);
	newPa.setAnchorTime(anchorTime);
	newPa.setBeginPoint(beginPoint);
	newPa.setEndPoint(endPoint);
	return newPa;
    }

    public CLink newCLink(Predicate from, Predicate to) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.CLINK);
	return this.newCLink(newId, from, to);
    }
    
    public CLink newCLink(String id, Predicate from, Predicate to) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.CLINK, id);
	CLink newCLink = new CLink(this.annotationContainer, id, from, to);
	annotationContainer.add(newCLink, AnnotationType.CLINK);
	return newCLink;
    }

    public Factuality newFactuality(Span<Term> span) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.FACTUALITY);
	return this.newFactuality(newId, span);
    }

    public Factuality newFactuality(String id, Span<Term> span) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.FACTUALITY, id);
	Factuality newFactuality= new Factuality(this.annotationContainer, id, span);
	annotationContainer.add(newFactuality, AnnotationType.FACTUALITY);
	return newFactuality;
    }

    public Factuality.FactVal newFactVal(String value, String resource) {
	return new Factuality.FactVal(this.annotationContainer, value, resource);
    }
    
	/** Creates a factualitylayer object and add it to the document
	 * @param term the Term of the coreference.
	 * @return a new factuality.
	 */
    public Factvalue newFactvalue(WF wf, String prediction) {
	Factvalue factuality = new Factvalue(this.annotationContainer, wf, prediction);
	annotationContainer.add(factuality, AnnotationType.FACTVALUE);
	return factuality;
    }

	/** Creates a LinkedEntity object and add it to the document
	 * @param term the Term of the coreference.
	 * @return a new factuality.
	 */
    /*
	public LinkedEntity newLinkedEntity(Span<WF> span) {
		String newId = annotationContainer.getIdManager().getNextId(Annotations.LINKED_ENTITY);
		LinkedEntity linkedEntity = new LinkedEntity(newId, span);
		annotationContainer.add(linkedEntity);
		return linkedEntity;
	}
    */

    /** Creates a new property. It assigns an appropriate ID to it. The property is added to the document.
     * @param lemma the lemma of the property.
     * @param references different mentions (list of targets) to the same property.
     * @return a new coreference.
     */
    public Feature newProperty(String lemma, List<Span<Term>> references) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.PROPERTY);
	return this.newProperty(newId, lemma, references);
    }

    /** Creates a new property. It receives it's ID as an argument. The property is added to the document.
     * @param id the ID of the property.
     * @param lemma the lemma of the property.
     * @param references different mentions (list of targets) to the same property.
     * @return a new coreference.
     */
    public Feature newProperty(String id, String lemma, List<Span<Term>> references) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.PROPERTY, id);
	Feature newProperty = new Feature(this.annotationContainer, id, lemma, references);
	annotationContainer.add(newProperty, AnnotationType.PROPERTY);
	return newProperty;
    }

    /** Creates a new category. It assigns an appropriate ID to it. The category is added to the document.
     * @param lemma the lemma of the category.
     * @param references different mentions (list of targets) to the same category.
     * @return a new coreference.
     */
    public Feature newCategory(String lemma, List<Span<Term>> references) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.CATEGORY);
	return this.newCategory(newId, lemma, references);
    }

    /** Creates a new category. It receives it's ID as an argument. The category is added to the document.
     * @param id the ID of the category.
     * @param lemma the lemma of the category.
     * @param references different mentions (list of targets) to the same category.
     * @return a new coreference.
     */
    public Feature newCategory(String id, String lemma, List<Span<Term>> references) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.CATEGORY, id);
	Feature newCategory = new Feature(this.annotationContainer, id, lemma, references);
	annotationContainer.add(newCategory, AnnotationType.CATEGORY);
	return newCategory;
    }
    
    /** Creates a new opinion object. It assigns an appropriate ID to it. The opinion is added to the document.
     * @return a new opinion.
     */
    public Opinion newOpinion() {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.OPINION);
	return this.newOpinion(newId);
    }

    /** Creates a new opinion object. It receives its ID as an argument. The opinion is added to the document.
     * @return a new opinion.
     */
    public Opinion newOpinion(String id) {
        annotationContainer.getIdManager().updateCounter(AnnotationType.OPINION, id);
	Opinion newOpinion = new Opinion(this.annotationContainer, id);
	annotationContainer.add(newOpinion, AnnotationType.OPINION);
	return newOpinion;
    }

    /** Creates a new relation between entities and/or sentiment features. It assigns an appropriate ID to it. The relation is added to the document.
     * @param from source of the relation
     * @param to target of the relation
     * @return a new relation
     */
    public Relation newRelation(Relational from, Relational to) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.RELATION);
	return this.newRelation(newId, from, to);
    }
    
    /** Creates a new relation between entities and/or sentiment features. It receives its ID as an argument. The relation is added to the document.
     * @param id the ID of the relation
     * @param from source of the relation
     * @param to target of the relation
     * @return a new relation
     */
    public Relation newRelation(String id, Relational from, Relational to) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.RELATION, id);
	Relation newRelation = new Relation(this.annotationContainer, id, from, to);
	annotationContainer.add(newRelation, AnnotationType.RELATION);
	return newRelation;
    }

    /** Creates a new srl predicate. It assigns an appropriate ID to it. The predicate is added to the document.
     * @param span span containing all the targets of the predicate
     * @return a new predicate
     */
    public Predicate newPredicate(Span<Term> span) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.PREDICATE);
	return this.newPredicate(newId, span);
    }
    
    /** Creates a new srl predicate. It receives its ID as an argument. The predicate is added to the document.
     * @param id the ID of the predicate
     * @param span span containing the targets of the predicate
     * @return a new predicate
     */
    public Predicate newPredicate(String id, Span<Term> span) {
	    annotationContainer.getIdManager().updateCounter(AnnotationType.PREDICATE, id);
	Predicate newPredicate = new Predicate(this.annotationContainer, id, span);
	annotationContainer.add(newPredicate, AnnotationType.PREDICATE);
	return newPredicate;
    }

    /** Creates a new Role object. It assigns an appropriate ID to it. It uses the ID of the predicate to create a new ID for the role. It doesn't add the role to the predicate.
     * @param predicate the predicate which this role is part of
     * @param semRole semantic role
     * @param span span containing all the targets of the role
     * @return a new role.
     */
    public Predicate.Role newRole(Predicate predicate, String semRole, Span<Term> span) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.ROLE);
	return this.newRole(newId, predicate, semRole, span);
    }

    /** Creates a Role object to load an existing role. It receives the ID as an argument. It doesn't add the role to the predicate.
     * @param id role's ID.
     * @param predicate the predicate which this role is part of
     * @param semRole semantic role
     * @param span span containing all the targets of the role
     * @return a new role.
     */
    public Predicate.Role newRole(String id, Predicate predicate, String semRole, Span<Term> span) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.ROLE, id);
	Predicate.Role newRole = new Predicate.Role(this.annotationContainer, id, semRole, span);
	return newRole;
    }

    /** Creates a new external reference.
     * @param resource indicates the identifier of the resource referred to.
     * @param reference code of the referred element.
     * @return a new external reference object.
     */
    public ExternalRef newExternalRef(String resource, String reference) {
	return new ExternalRef(resource, reference);
    }
    
    public ExternalRef newExternalRef(String resource) {
	return new ExternalRef(resource, null);
    }

    public Tree newConstituent(TreeNode root) {
	return this.newConstituent(root, null);
    }

    public Tree newConstituent(TreeNode root, String type) {
	Tree tree = new Tree(this.annotationContainer, root, type);
	annotationContainer.add(tree, AnnotationType.TREE);
	return tree;
    }

    public void newConstituentFromParentheses(String parseOut) throws Exception {
	Tree.parenthesesToKaf(parseOut, this);
    }
    
    public NonTerminal newNonTerminal(String label) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.NON_TERMINAL);
	return this.newNonTerminal(newId, label);
    }
    
    public NonTerminal newNonTerminal(String id, String label) {
	NonTerminal tn = new NonTerminal(this.annotationContainer, id, label);
	String newEdgeId = annotationContainer.getIdManager().getNextId(AnnotationType.EDGE);
	tn.setEdgeId(newEdgeId);
	return tn;
    }

    public Terminal newTerminal(Span<Term> span) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.TERMINAL);
	return this.newTerminal(newId, span);
    }

    public Terminal newTerminal(String id, Span<Term> span) {
	Terminal tn = new Terminal(this.annotationContainer, id, span);
	String newEdgeId = annotationContainer.getIdManager().getNextId(AnnotationType.EDGE);
	tn.setEdgeId(newEdgeId);
	return tn;
    }

    public Topic newTopic(String value) {
	Topic newTopic = new Topic(this.annotationContainer, value);
	annotationContainer.add(newTopic, AnnotationType.TOPIC);
	return newTopic;
    }
    
    public Statement newStatement(Statement.StatementTarget target) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.STATEMENT);
	return this.newStatement(newId, target);
    }
    
    public Statement newStatement(String id, Statement.StatementTarget target) {
        annotationContainer.getIdManager().updateCounter(AnnotationType.STATEMENT, id);
	Statement newStatement = new Statement(this.annotationContainer, id, target);
	annotationContainer.add(newStatement, AnnotationType.STATEMENT);
	return newStatement;
    }
    
    public Statement.StatementTarget newStatementTarget(Span<Term> span) {
	return new Statement.StatementTarget(this.annotationContainer, span);
    }
    
    public Statement.StatementSource newStatementSource(Span<Term> span) {
	return new Statement.StatementSource(this.annotationContainer, span);
    }
    
    public Statement.StatementCue newStatementCue(Span<Term> span) {
	return new Statement.StatementCue(this.annotationContainer, span);
    }

    public static <T extends IdentifiableAnnotation> Span<T> newSpan() {
	return new Span<T>();
    }

    public static <T extends IdentifiableAnnotation> Span<T> newSpan(List<T> targets) {
	return new Span<T>(targets);
    }

    void addUnknownLayer(Element layer) {
	annotationContainer.addUnknownLayer(layer);
    }

    
    /** Returns the raw text **/
    public String getRawText() {
	return this.annotationContainer.getRawText();
    }
    
    public List<Annotation> getAnnotations(AnnotationType type) {
	return this.annotationContainer.getAnnotations(type);
    }

    public List<Annotation> getAnnotations(Layer layer) {
	return this.annotationContainer.getAnnotations(layer);
    }
    
    public Annotation getAnnotationById(String id) {
	return this.annotationContainer.getAnnotationById(id);
    }
    
    public List<Annotation> getAnnotationsBy(AnnotationType returnType, Annotation annotation) {
	return this.annotationContainer.getAnnotationsBy(annotation, returnType);
    }
    
    public List<Annotation> getAnnotationsBy(AnnotationType returnType, List<Annotation> annotations) {
	return this.annotationContainer.getAnnotationsBy(annotations, returnType);
    }
    
    public List<Annotation> getAnnotationsBy(Layer returnLayer, Annotation annotation) {
	return this.annotationContainer.getAnnotationsBy(annotation, returnLayer);
    }

    public List<Annotation> getAnnotationsBy(Layer returnLayer, List<Annotation> annotations) {
	return this.annotationContainer.getAnnotationsBy(annotations, returnLayer);
    }

    public List<List<WF>> getSentences() {
	return (List<List<WF>>)(List<?>) this.annotationContainer.getAnnotationsBySentences(AnnotationType.WF);
    }

    public List<List<Annotation>> getSentences(AnnotationType type) {
	return this.annotationContainer.getAnnotationsBySentences(type);
    }

    public List<List<Annotation>> getSentences(Layer layer) {
	return this.annotationContainer.getAnnotationsBySentences(layer);
    }
    
    public List<List<WF>> getParagraphs() {
	return (List<List<WF>>)(List<?>) this.annotationContainer.getAnnotationsByParagraphs(AnnotationType.WF);
    }
    
    public List<List<Annotation>> getParagraphs(AnnotationType type) {
	return this.annotationContainer.getAnnotationsByParagraphs(type);
    }
    
    public List<List<Annotation>> getParagraphs(Layer layer) {
	return this.annotationContainer.getAnnotationsByParagraphs(layer);
    }

    public List<Annotation> getBySent(AnnotationType type, Integer sent) {
	return this.annotationContainer.getAnnotationsBySent(sent, type);
    }

    public List<Annotation> getBySent(Layer layer, Integer sent) {
	return this.annotationContainer.getAnnotationsBySent(sent, layer);
    }

    public List<Annotation> getByPara(AnnotationType type, Integer para) {
	return this.annotationContainer.getAnnotationsByPara(para, type);
    }

    public List<Annotation> getByPara(Layer layer, Integer para) {
	return this.annotationContainer.getAnnotationsByPara(para, layer);
    }

    public Integer getFirstSentence() {
	return this.annotationContainer.getFirstSentence();
    }

    public Integer getNumSentences() {
	return this.annotationContainer.getNumSentences();
    }
    
    public Integer getParagraph() {
	return this.getFirstParagraph();
    }
    
    public Integer getSentence() {
	return this.getFirstSentence();
    }

    public Integer getFirstParagraph() {
	return this.annotationContainer.getFirstParagraph();
    }

    public Integer getNumParagraphs() {
	return this.annotationContainer.getNumParagraphs();
    }

    public List<Integer> getSentsByParagraph(Integer para) {
	return this.annotationContainer.getSentsByPara(para);
    }

    public Set<Element> getUnknownLayers() {
	return annotationContainer.getUnknownLayers();
    }

    
    public List<KAFDocument> splitInSentences()
    {
	List<KAFDocument> sentNafs = new ArrayList<KAFDocument>();
	Integer numParagraphs = this.getNumParagraphs();
	for (Integer paragraph = 1; paragraph <= numParagraphs; paragraph++) {
	    List<Integer> sentences = this.getSentsByParagraph(paragraph);
	    for (Integer sentence : sentences) {
		KAFDocument naf = new KAFDocument(this.getLang(), this.getVersion());
		naf.setRawText(this.getRawText());
		for (AnnotationType type : TOP_TYPES) {
		    if (isSentenceLevelAnnotationType(type)) {
			List<Annotation> annotations = this.getBySent(type, sentence);
			for (Annotation ann : annotations) {
			    naf.addExistingAnnotation(ann, type);
			}
		    }
		}
		sentNafs.add(naf);
	    }
	}
	return sentNafs;
    }

    public List<KAFDocument> splitInParagraphs()
    {
	List<KAFDocument> paraNafs = new ArrayList<KAFDocument>();
	Integer numParagraphs = this.getNumParagraphs();
	for (Integer paragraph = 1; paragraph <= numParagraphs; paragraph++) {
	    KAFDocument naf = new KAFDocument(this.getLang(), this.getVersion());
	    naf.setRawText(this.getRawText());
	    for (AnnotationType type : TOP_TYPES) {
		if (isParagraphLevelAnnotationType(type)) {
		    List<Annotation> annotations = this.getByPara(type, paragraph);
		    for (Annotation ann : annotations) {
			naf.addExistingAnnotation(ann, type);
		    }
		}
	    }
	    paraNafs.add(naf);
	}
	return paraNafs;
    }
    
    public static KAFDocument join(List<KAFDocument> nafs)
    {
	KAFDocument firstNaf = nafs.get(0);
	KAFDocument joinedNaf = new KAFDocument(firstNaf.getLang(), nafs.get(0).getVersion());
	joinedNaf.setRawText(firstNaf.getRawText());
	for (KAFDocument nafPart : nafs) {
	    for (AnnotationType type : TOP_TYPES) {
		List<Annotation> annotations = nafPart.getAnnotations(type);
		for (Annotation ann : annotations) {
		    joinedNaf.addExistingAnnotation(ann, type);
		}
	    }
	}
	return joinedNaf;
    }

    public void addExistingAnnotation(Annotation ann, AnnotationType type) {
	if (isIdentifiableAnnotationType(type)) {
	    String newId = annotationContainer.getIdManager().getNextId(type);
	    ((IdentifiableAnnotation) ann).setId(newId);
	}
	annotationContainer.add(ann, type);
    }

    private static Boolean isSentenceLevelAnnotationType(AnnotationType type) {
	Class<?> annotationClass = TYPE_2_CLASS.get(type);
	if (annotationClass == null) return false;
	return SentenceLevelAnnotation.class.isAssignableFrom(annotationClass);
    }
    
    private static Boolean isParagraphLevelAnnotationType(AnnotationType type) {
	Class<?> annotationClass = TYPE_2_CLASS.get(type);
	if (annotationClass == null) return false;
	return ParagraphLevelAnnotation.class.isAssignableFrom(annotationClass);
    }
    
    private static Boolean isIdentifiableAnnotationType(AnnotationType type) {
	Class<?> annotationClass = TYPE_2_CLASS.get(type);
	if (annotationClass == null) return false;
	return IdentifiableAnnotation.class.isAssignableFrom(annotationClass);
    }

    /** Returns current timestamp. */
    public String createTimestamp() {
	Date date = new Date();
	//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:mm:ss");
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	String formattedDate = sdf.format(date);
	return formattedDate;
    }

    public String toString() {
	return ReadWriteManager.kafToStr(this);
    }

    /** Prints the document on standard output. */
    public void print() {
	ReadWriteManager.print(this);
    }


    
    /**************************/
    /*** DEPRECATED METHODS ***/
    /**************************/

    /** Deprecated */
    public LinguisticProcessor addLinguisticProcessor(String layer, String name, String version) {
        LinguisticProcessor lp = this.addLinguisticProcessor(layer, name);
	lp.setVersion(version);
	return lp;
    }

    /** Deprecated */
    public LinguisticProcessor addLinguisticProcessor(String layer, String name, String timestamp, String version) {
	LinguisticProcessor lp = this.addLinguisticProcessor(layer, name);
	lp.setTimestamp(timestamp);
	lp.setVersion(version);
	return lp;
    }

    @Deprecated
    public WF newWF(String id, int offset, int length, String form, int sent) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.WF, id);
	WF newWF = new WF(this.annotationContainer, id, offset, length, form, sent);
	annotationContainer.add(newWF, AnnotationType.WF);
	return newWF;
    }

    @Deprecated
    public WF newWF(int offset, Integer length, String form, int sent) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.WF);
	WF newWF = new WF(this.annotationContainer, newId, offset, length, form, sent);
	annotationContainer.add(newWF, AnnotationType.WF);
	return newWF;
    }
    
    /** Deprecated */
    public Term newTerm(String id, String type, String lemma, String pos, Span<WF> span) {
        Term term = newTerm(id, span);
	term.setType(type);
	term.setLemma(lemma);
	term.setPos(pos);
	return term;
    }

    /** Deprecated */
    public Term newTerm(String type, String lemma, String pos, Span<WF> span) {
        Term term = newTerm(span);
	term.setType(type);
	term.setLemma(lemma);
	term.setPos(pos);
	return term;
    }

    /** Deprecated */
    public Term newTermOptions(String type, String lemma, String pos, String morphofeat, Span<WF> span) {
	Term newTerm = newTermOptions(morphofeat, span);
	newTerm.setType(type);
	newTerm.setLemma(lemma);
	newTerm.setPos(pos);
	return newTerm;
    }

    /** Deprecated */
    public Term createTerm(String id, String type, String lemma, String pos, List<WF> wfs) {
	return this.newTerm(id, type, lemma, pos, this.<WF>list2Span(wfs));
    }

    /** Deprecated */
    public Term createTerm(String type, String lemma, String pos, List<WF> wfs) {
        return this.newTerm(type, lemma, pos, this.<WF>list2Span(wfs));
    }

    /** Deprecated */
    public Term createTermOptions(String type, String lemma, String pos, String morphofeat, List<WF> wfs) {
	return this.newTermOptions(type, lemma, pos, morphofeat, this.<WF>list2Span(wfs));
    }

    @Deprecated
    public Term newTerm(String id, Span<WF> span, Integer position) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.TERM, id);
	Term newTerm = new Term(this.annotationContainer, id, span);
	annotationContainer.addAt(newTerm, AnnotationType.TERM, position);
	return newTerm;
    }

    @Deprecated
    public Term newTermOptions(String morphofeat, Span<WF> span) {
	String newId = annotationContainer.getIdManager().getNextId(AnnotationType.TERM);
	Term newTerm = new Term(this.annotationContainer, newId, span);
	newTerm.setMorphofeat(morphofeat);
	annotationContainer.add(newTerm, AnnotationType.TERM);
	return newTerm;
    }

    @Deprecated
    public Term newTerm(Span<WF> span, boolean isComponent) {
	return this.newTerm(span);
    }

    @Deprecated
    public Term newTerm(String id, Span<WF> span, boolean isComponent) {
	return this.newTerm(id, span);
    }

    /** Deprecated */
    public Term.Sentiment createSentiment() {
	return this.newSentiment();
    }

    /** Deprecated */
    public Dep createDep(Term from, Term to, String rfunc) {
	return this.createDep(from, to, rfunc);
    }

    /** Deprecated */
    public Chunk createChunk(String id, Term head, String phrase, List<Term> terms) {
	return this.newChunk(id, phrase, this.<Term>list2Span(terms, head));
    }

    /** Deprecated */
    public Chunk createChunk(Term head, String phrase, List<Term> terms) {
	return this.newChunk(phrase, this.<Term>list2Span(terms, head));
    }

    /** Deprecated */
    public Entity createEntity(String id, String type, List<List<Term>> references) {
	List<Span<Term>> spanReferences = new ArrayList<Span<Term>>();
	for (List<Term> list : references) {
	    spanReferences.add(this.list2Span(list));
	}
	Entity entity = this.newEntity(id, spanReferences);
	entity.setType(type);
	return entity;
    }

    /** Deprecated */
    public Entity createEntity(String type, List<List<Term>> references) {
	List<Span<Term>> spanReferences = new ArrayList<Span<Term>>();
	for (List<Term> list : references) {
	    spanReferences.add(this.list2Span(list));
	}
	Entity entity = this.newEntity(spanReferences);
	entity.setType(type);
	return entity;
    }

    /** Deprecated */
    public Coref createCoref(String id, List<List<Target>> references) {
	List<Span<Term>> spanReferences = new ArrayList<Span<Term>>();
	for (List<Target> list : references) {
	    spanReferences.add(this.targetList2Span(list));
	}
	return this.newCoref(id, spanReferences);
    }

    /** Deprecated */
    public Coref createCoref(List<List<Target>> references) {
	List<Span<Term>> spanReferences = new ArrayList<Span<Term>>();
	for (List<Target> list : references) {
	    spanReferences.add(this.targetList2Span(list));
	}
	return this.newCoref(spanReferences);
    }
    
    @Deprecated
    public Mark newMark(Span<WF> span, String source) {
	Mark newMark = this.newMark(span);
	newMark.setSource(source);
	return newMark;
    }

    @Deprecated
    public Mark newMark(String id, String source, Span<WF> span) {
	annotationContainer.getIdManager().updateCounter(AnnotationType.MARK, id);
	Mark newMark = new Mark(this.annotationContainer, id, span);
	newMark.setSource(source);
	annotationContainer.add(newMark, AnnotationType.MARK);
	return newMark;
    }

    /** Deprecated */
    public Opinion createOpinion() {
	return this.newOpinion();
    }

    /** Deprecated */
    public Opinion createOpinion(String id) {
	return this.newOpinion(id);
    }

    @Deprecated
    public void addConstituencyFromParentheses(String parseOut) throws Exception {
	this.newConstituentFromParentheses(parseOut);
    }
    
    /** Deprecated */
    public ExternalRef createExternalRef(String resource, String reference) {
	return this.newExternalRef(resource, reference);
    }

    /** Deprecated. Creates a new target. This method is overloaded. Any target created by calling this method won't be the head term.
     * @param term target term.
     * @return a new target.
     */
    public static Target createTarget(Term term) {
	return new Target(term, false);
    }

    /** Deprecated. Creates a new target. This method is overloaded. In this case, it receives a boolean argument which defines whether the target term is the head or not.
     * @param term target term.
     * @param isHead a boolean argument which defines whether the target term is the head or not.
     * @return a new target.
     */
    public static Target createTarget(Term term, boolean isHead) {
	return new Target(term, isHead);
    }

    /** Returns a list containing all WFs in the document */
    @Deprecated
    public List<WF> getWFs() {
	return (List<WF>)(List<?>) this.getAnnotations(AnnotationType.WF);
    }

    /** Returns a list with all terms in the document. */
    @Deprecated
    public List<Term> getTerms() {
	return (List<Term>)(List<?>) this.getAnnotations(AnnotationType.TERM);
    }

    /** Returns a list with all entities in the document */
    @Deprecated
    public List<Entity> getEntities() {
	return (List<Entity>)(List<?>) this.getAnnotations(AnnotationType.ENTITY);
    }

    @Deprecated
    public List<Chunk> getChunks() {
	return (List<Chunk>)(List<?>) this.getAnnotations(AnnotationType.CHUNK);
    }

    @Deprecated
    public List<Dep> getDeps() {
	return (List<Dep>)(List<?>) this.getAnnotations(AnnotationType.DEP);
    }

    @Deprecated
    public List<Tree> getConstituents() {
	return (List<Tree>)(List<?>) this.getAnnotations(AnnotationType.TREE);
    }

    @Deprecated
    public List<Coref> getCorefs() {
	return (List<Coref>)(List<?>) this.getAnnotations(AnnotationType.COREF);
    }

    @Deprecated
    public List<Opinion> getOpinions() {
	return (List<Opinion>)(List<?>) this.getAnnotations(AnnotationType.OPINION);
    }

    @Deprecated
    public List<CLink> getCLinks() {
	return (List<CLink>)(List<?>) this.getAnnotations(AnnotationType.CLINK);
    }

    @Deprecated
    public List<TLink> getTLinks() {
	return (List<TLink>)(List<?>) this.getAnnotations(AnnotationType.TLINK);
    }

    @Deprecated
    public List<PredicateAnchor> getPredicateAnchors() {
	return (List<PredicateAnchor>)(List<?>) this.getAnnotations(AnnotationType.PREDICATE_ANCHOR);
    }

    @Deprecated
    public List<Predicate> getPredicates() {
	return (List<Predicate>)(List<?>) this.getAnnotations(AnnotationType.PREDICATE);
    }

    @Deprecated
    public List<Timex3> getTimeExs() {
	return (List<Timex3>)(List<?>) this.getAnnotations(AnnotationType.TIMEX3);
    }

    @Deprecated
    public List<Factuality> getFactualities() {
	return (List<Factuality>)(List<?>) this.getAnnotations(AnnotationType.FACTUALITY);
    }

    @Deprecated
    public List<Factvalue> getFactvalues() {
	return (List<Factvalue>)(List<?>) this.getAnnotations(AnnotationType.FACTVALUE);
    }

    @Deprecated
    public List<Mark> getMarks() {
	return (List<Mark>)(List<?>) this.getAnnotations(AnnotationType.MARK);
    }

    /** Returns a list with all relations in the document */
    @Deprecated
    public List<Feature> getProperties() {
	return (List<Feature>)(List<?>) this.getAnnotations(AnnotationType.PROPERTY);
    }

    /** Returns a list with all relations in the document */
    @Deprecated
    public List<Feature> getCategories() {
	return (List<Feature>)(List<?>) this.getAnnotations(AnnotationType.CATEGORY);
    }

    @Deprecated
    public List<LinkedEntity> getLinkedEntities() {
	return (List<LinkedEntity>)(List<?>) this.getAnnotations(AnnotationType.LINKED_ENTITY);
    }

    /** Returns a list with all relations in the document */
    @Deprecated
    public List<Relation> getRelations() {
	return (List<Relation>)(List<?>) this.getAnnotations(AnnotationType.RELATION);
    }

    @Deprecated
    public List<Topic> getTopics() {
	return (List<Topic>)(List<?>) this.getAnnotations(AnnotationType.TOPIC);
    }

    @Deprecated
    public List<Statement> getStatements() {
	return (List<Statement>)(List<?>) this.getAnnotations(AnnotationType.STATEMENT);
    }

    @Deprecated
    public List<WF> getWFsBySent(Integer sent) {
	return (List<WF>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.WF);
    }

    @Deprecated
    public List<WF> getWFsByPara(Integer para) {
	return (List<WF>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.WF);
    }

    @Deprecated
    public List<Term> getTermsBySent(Integer sent) {
	return (List<Term>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.TERM);
    }

    @Deprecated
    public List<Term> getTermsByPara(Integer para) {
	return (List<Term>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.TERM);
    }

    @Deprecated
    public List<Entity> getEntitiesBySent(Integer sent) {
	return (List<Entity>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.ENTITY);
    }

    @Deprecated
    public List<Entity> getEntitiesByPara(Integer para) {
	return (List<Entity>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.ENTITY);
    }

    @Deprecated
    public List<Chunk> getChunksBySent(Integer sent) {
	return (List<Chunk>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.CHUNK);
    }

    @Deprecated
    public List<Chunk> getChunksByPara(Integer para) {
	return (List<Chunk>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.CHUNK);
    }

    @Deprecated
    public List<Dep> getDepsBySent(Integer sent) {
	return (List<Dep>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.DEP);
    }

    @Deprecated
    public List<Dep> getDepsByPara(Integer para) {
	return (List<Dep>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.DEP);
    }

    @Deprecated
    public List<Tree> getConstituentsBySent(Integer sent) {
	return (List<Tree>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.TREE);
    }

    @Deprecated
    public List<Tree> getConstituentsByPara(Integer para) {
	return (List<Tree>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.TREE);
    }

    @Deprecated
    public List<Tree> getConstituentsBySent(Integer sent, String treeType) {
	return (List<Tree>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.TREE);
    }

    @Deprecated
    public List<Tree> getConstituentsByPara(Integer para, String treeType) {
	return (List<Tree>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.TREE);
    }

    @Deprecated
    public List<Coref> getCorefsBySent(Integer sent) {
	return (List<Coref>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.COREF);
    }

    @Deprecated
    public List<Coref> getCorefsByPara(Integer para) {
	return (List<Coref>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.COREF);
    }

    @Deprecated
    public List<Opinion> getOpinionsBySent(Integer sent) {
	return (List<Opinion>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.OPINION);
    }

    @Deprecated
    public List<Opinion> getOpinionsByPara(Integer para) {
	return (List<Opinion>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.OPINION);
    }

    @Deprecated
    public List<CLink> getCLinksBySent(Integer sent) {
	return (List<CLink>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.CLINK);
    }

    @Deprecated
    public List<CLink> getCLinksByPara(Integer para) {
	return (List<CLink>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.CLINK);
    }

    @Deprecated
    public List<TLink> getTLinksBySent(Integer sent) {
	return (List<TLink>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.TLINK);
    }

    @Deprecated
    public List<TLink> getTLinksByPara(Integer para) {
	return (List<TLink>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.TLINK);
    }

    @Deprecated
    public List<PredicateAnchor> getPredicateAnchorsBySent(Integer sent) {
	return (List<PredicateAnchor>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.PREDICATE_ANCHOR);
    }

    @Deprecated
    public List<PredicateAnchor> getPredicateAnchorsByPara(Integer para) {
	return (List<PredicateAnchor>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.PREDICATE_ANCHOR);
    }

    @Deprecated
    public List<Predicate> getPredicatesBySent(Integer sent) {
	return (List<Predicate>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.PREDICATE);
    }

    @Deprecated
    public List<Predicate> getPredicatesByPara(Integer para) {
	return (List<Predicate>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.PREDICATE);
    }

    @Deprecated
    public List<Timex3> getTimeExsBySent(Integer sent) {
	return (List<Timex3>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.TIMEX3);
    }

    @Deprecated
    public List<Timex3> getTimeExsByPara(Integer para) {
	return (List<Timex3>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.TIMEX3);
    }

    @Deprecated
    public List<Factuality> getFactualitiesBySent(Integer sent) {
	return (List<Factuality>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.FACTUALITY);
    }

    @Deprecated
    public List<Factuality> getFactualitiesByPara(Integer para) {
	return (List<Factuality>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.FACTUALITY);
    }

    @Deprecated
    public List<Factvalue> getFactvaluesBySent(Integer sent) {
	return (List<Factvalue>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.FACTVALUE);
    }

    @Deprecated
    public List<Factvalue> getFactvaluesByPara(Integer para) {
	return (List<Factvalue>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.FACTVALUE);
    }

    @Deprecated
    public List<Mark> getMarksBySent(Integer sent) {
	return (List<Mark>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.MARK);
    }

    @Deprecated
    public List<Mark> getMarksByPara(Integer para) {
	return (List<Mark>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.MARK);
    }

    @Deprecated
    public List<Mark> getMarksBySent(Integer sent, String group) {
	return (List<Mark>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.MARK);
    }

    @Deprecated
    public List<Mark> getMarksByPara(Integer para, String group) {
	return (List<Mark>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.MARK);
    }

    @Deprecated
    public List<Feature> getPropertiesBySent(Integer sent) {
	return (List<Feature>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.PROPERTY);
    }

    @Deprecated
    public List<Feature> getPropertiesByPara(Integer para) {
	return (List<Feature>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.PROPERTY);
    }

    @Deprecated
    public List<Feature> getCategoriesBySent(Integer sent) {
	return (List<Feature>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.CATEGORY);
    }

    @Deprecated
    public List<Feature> getCategoriesByPara(Integer para) {
	return (List<Feature>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.CATEGORY);
    }

    @Deprecated
    public List<LinkedEntity> getLinkedEntitiesBySent(Integer sent) {
	return (List<LinkedEntity>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.LINKED_ENTITY);
    }

    @Deprecated
    public List<LinkedEntity> getLinkedEntitiesByPara(Integer para) {
	return (List<LinkedEntity>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.LINKED_ENTITY);
    }

    @Deprecated
    public List<Relation> getRelationsBySent(Integer sent) {
	return (List<Relation>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.RELATION);
    }

    @Deprecated
    public List<Relation> getRelationsByPara(Integer para) {
	return (List<Relation>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.RELATION);
    }

    @Deprecated
    public List<Topic> getTopicsBySent(Integer sent) {
	return (List<Topic>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.TOPIC);
    }

    @Deprecated
    public List<Topic> getTopicsByPara(Integer para) {
	return (List<Topic>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.TOPIC);
    }

    @Deprecated
    public List<Statement> getStatementsBySent(Integer sent) {
	return (List<Statement>)(List<?>) this.annotationContainer.getAnnotationsBySent(sent, AnnotationType.STATEMENT);
    }

    @Deprecated
    public List<Statement> getStatementsByPara(Integer para) {
	return (List<Statement>)(List<?>) this.annotationContainer.getAnnotationsByPara(para, AnnotationType.STATEMENT);
    }
    
    @Deprecated
    public List<Term> getTermsFromWFs(List<String> wfIds) {
	List<Annotation> wfs = new ArrayList<Annotation>();
	for (String id : wfIds) {
	    Annotation wf = this.getAnnotationById(id);
	    if (wf != null) wfs.add(wf);
	}
	return (List<Term>)(List<?>) this.getAnnotationsBy(AnnotationType.TERM, wfs);
    }
    
    public static Span<WF> newWFSpan() {
	return new Span<WF>();
    }

    public static Span<WF> newWFSpan(List<WF> targets) {
	return new Span<WF>(targets);
    }

    public static Span<WF> newWFSpan(List<WF> targets, WF head) {
	return new Span<WF>(targets, head);
    }

    public static Span<Term> newTermSpan() {
	return new Span<Term>();
    }
    
    public static Span<Term> newTermSpan(List<Term> targets) {
	return new Span<Term>(targets);
    }

    public static Span<Term> newTermSpan(List<Term> targets, Term head) {
	return new Span<Term>(targets, head);
    }

    public void removeLayer(Layer layer) {
	this.annotationContainer.remove(layer);
    }

    /** Converts a List into a Span */
    static <T extends IdentifiableAnnotation> Span<T> list2Span(List<T> list) {
	Span<T> span = new Span<T>();
	for (T elem : list) {
	    span.addTarget(elem);
	}
	return span;
    }

    /** Converts a List into a Span */
    static <T extends IdentifiableAnnotation> Span<T> list2Span(List<T> list, T head) {
	Span<T> span = new Span<T>();
	for (T elem : list) {
	    if (head == elem) {
		span.addTarget(elem, true);
	    } else {
		span.addTarget(elem);
	    }
	}
	return span;
    }

    /** Converts a Target list into a Span of terms */
    static Span<Term> targetList2Span(List<Target> list) {
	Span<Term> span = new Span<Term>();
	for (Target target : list) {
	    if (target.isHead()) {
		span.addTarget(target.getTerm(), true);
	    } else {
		span.addTarget(target.getTerm());
	    }
	}
	return span;
    }

    /** Converts a Span into a Target list */
    static List<Target> span2TargetList(Span<Term> span) {
	List<Target> list = new ArrayList<Target>();
	for (Term t : span.getTargets()) {
	    list.add(KAFDocument.createTarget(t, (t==span.getHead())));
	}
	return list;
    }
    
    /** Retrieve the term at position index.
     * @param index the global index of the term in the document, starting at zero.
     * @return the required term.
     */
    public Term termNth(Integer index) {
        return this.getTerms().get(index) ;
    }

    public List<Entity> getEntitiesByTerm(Term term) {
	return (List<Entity>)(List<?>) this.annotationContainer.getAnnotationsBy(term, AnnotationType.ENTITY);
    }

    public List<Predicate> getPredicatesByTerm(Term term) {
	return (List<Predicate>)(List<?>) this.annotationContainer.getAnnotationsBy(term, AnnotationType.PREDICATE);
    }
    
    public List<Term> getSentenceTerms(int sent) {
	return (List<Term>)(List<?>) annotationContainer.getAnnotationsBySent(sent, AnnotationType.TERM);
    }
    
    /** Saves the KAF document to an XML file.
     * @param filename name of the file in which the document will be saved.
     */
    @Deprecated
    public void save(String filename) {
	ReadWriteManager.save(this, filename);
    }
    
    
    @Override
    public boolean equals(Object o) {
	if (this == o) return true;
	if (!(o instanceof KAFDocument)) return false;
	KAFDocument naf = (KAFDocument) o;
	/* Language and version */
	if (!this.getLang().equals(naf.getLang()) || !this.getVersion().equals(naf.getVersion())) return false;
	/* NAF header */
	if (!this.headerEquals(naf)) return false;
	/* Layers and annotations */
	return Utils.areEquals(this.annotationContainer, naf.annotationContainer);
    }

    private Boolean headerEquals(KAFDocument naf) {
	return Utils.areEquals(this.fileDesc, naf.fileDesc) &&
		Utils.areEquals(this._public, naf._public) &&
		Utils.areEquals(this.lps, naf.lps);
    }
    
    
    static class Utils {
	static boolean areEquals(Object a, Object b) {
	    return (a == null) ? (b == null) : a.equals(b);
	}
    }

    
    
    /**************************************************/
    /*************** ADDED BY FRANCESCO ***************/
    /**************************************************/

    private static final Map<String, Character> DEP_PATH_CHARS = new HashMap<String, Character>();

    private static final Map<String, Pattern> DEP_PATH_REGEXS = new HashMap<String, Pattern>();

    private static char getDepPathChar(final String label) {
    final String key = label.toLowerCase();
    synchronized (DEP_PATH_CHARS) {
        Character letter = DEP_PATH_CHARS.get(key);
        if (letter == null) {
            letter = 'a';
            for (final Character ch : DEP_PATH_CHARS.values()) {
                if (ch >= letter) {
                    letter = (char) (ch + 1);
                }
            }
            DEP_PATH_CHARS.put(key, letter);
        }
        return letter;
    }
    }

    private static String getDepPathString(final Term from, final Iterable<Dep> path) {
    final StringBuilder builder = new StringBuilder("_");
    Term term = from; // current node in the path
    for (final Dep dep : path) {
        char prefix;
        if (dep.getFrom() == term) {
            prefix = '+';
            term = dep.getTo();
        } else {
            prefix = '-';
            term = dep.getFrom();
        }
        for (final String label : dep.getRfunc().split("-")) {
            final Character letter = getDepPathChar(label);
            builder.append(prefix).append(letter);
        }
        builder.append("_");
    }
    return builder.toString();
    }

    private static Pattern getDepPathRegex(final String pattern) {
    synchronized (DEP_PATH_REGEXS) {
        Pattern regex = DEP_PATH_REGEXS.get(pattern);
        if (regex == null) {
            final StringBuilder builder = new StringBuilder();
            builder.append('_');
            int start = -1;
            for (int i = 0; i < pattern.length(); ++i) {
                final char ch = pattern.charAt(i);
                if (Character.isLetter(ch) || ch == '-') {
                    if (start < 0) {
                        start = i;
                    }
                } else {
                    if (start >= 0) {
                        final boolean inverse = pattern.charAt(start) == '-';
                        final String label = pattern.substring(inverse ? start + 1 : start, i);
                        final char letter = getDepPathChar(label);
                        builder.append("([^_]*")
                                .append(Pattern.quote((inverse ? "-" : "+") + letter))
                                .append("[^_]*_)");
                        start = -1;
                    }
                    if (!Character.isWhitespace(ch)) {
                        builder.append(ch);
                    }
                }
            }
            regex = Pattern.compile(builder.toString());
            DEP_PATH_REGEXS.put(pattern, regex);
        }
        return regex;
    }
    }

    public boolean matchDepPath(final Term from, final Iterable<Dep> path, final String pattern) {
    String pathString = getDepPathString(from, path);
    Pattern pathRegex = getDepPathRegex(pattern);
    return pathRegex.matcher(pathString).matches();
    }

    public List<Dep> getDepPath(final Term from, final Term to) {
    if (from == to) {
        return Collections.emptyList();
    }
    final List<Dep> toPath = new ArrayList<Dep>();
    for (Dep dep = getDepToTerm(to); dep != null; dep = getDepToTerm(dep.getFrom())) {
        toPath.add(dep);
        if (dep.getFrom() == from) {
            Collections.reverse(toPath);
            return toPath;
        }
    }
    final List<Dep> fromPath = new ArrayList<Dep>();
    for (Dep dep = getDepToTerm(from); dep != null; dep = getDepToTerm(dep.getFrom())) {
        fromPath.add(dep);
        if (dep.getFrom() == to) {
            return fromPath;
        }
        for (int i = 0; i < toPath.size(); ++i) {
            if (dep.getFrom() == toPath.get(i).getFrom()) {
                for (int j = i; j >= 0; --j) {
                    fromPath.add(toPath.get(j));
                }
                return fromPath;
            }
        }
    }
    return null; // unconnected nodes
    }

    public List<Dep> getDepsFromTerm(final Term term) {
	final List<Dep> result = new ArrayList<Dep>();
	for (final Dep dep : (List<Dep>)(List<?>)annotationContainer.getAnnotationsBy(term, AnnotationType.DEP)) {
	    if (dep.getFrom() == term) {
		result.add(dep);
	    }
	}
	return result;
    }
    
    public Dep getDepToTerm(final Term term) {
    for (final Dep dep : getDepsByTerm(term)) {
        if (dep.getTo() == term) {
            return dep;
        }
    }
    return null;
    }
    
    public List<Dep> getDepsByTerm(final Term term) {
    return (List<Dep>)(List<?>) this.annotationContainer.getAnnotationsBy(term, AnnotationType.DEP);
    }
    
    public Term getTermsHead(Iterable<Term> descendents) {
    final Set<Term> termSet = new HashSet<Term>();
    for (Term term : descendents) {
        termSet.add(term);
    }
    Term root = null;
    for (final Term term : termSet) {
        final Dep dep = getDepToTerm(term);
        if (dep == null || !termSet.contains(dep.getFrom())) {
            if (root == null) {
                root = term;
            } else if (root != term) {
                return null;
            }
        }
    }
    return root;
    }

    public Set<Term> getTermsByDepAncestors(final Iterable<Term> ancestors) {
    final Set<Term> terms = new HashSet<Term>();
    final List<Term> queue = new LinkedList<Term>();
    for (final Term term : ancestors) {
        terms.add(term);
        queue.add(term);
    }
    while (!queue.isEmpty()) {
        final Term term = queue.remove(0);
        final List<Dep> deps = getDepsByTerm(term);
        for (final Dep dep : deps) {
            if (dep.getFrom() == term) {
                if (terms.add(dep.getTo())) {
                    queue.add(dep.getTo());
                }
            }
        }
    }
    return terms;
    }

    public Set<Term> getTermsByDepAncestors(final Iterable<Term> ancestors, final String pattern) {
    final Set<Term> result = new HashSet<Term>();
    for (final Term term : ancestors) {
        for (final Term descendent : getTermsByDepAncestors(Collections.singleton(term))) {
            final List<Dep> path = getDepPath(term, descendent);
            if (matchDepPath(term, path, pattern)) {
                result.add(descendent);
            }
        }
    }
    return result;
    }
    
    
    
    /*******************/
    /** Inner Classes **/
    /*******************/
    
    public class FileDesc implements Serializable {
	public String author;
	public String title;
	public String publisher;
	public String section;
	public String location;
	public String magazine;
	public String filename;
	public String filetype;
	public Integer pages;
	public String creationtime;
	private static final long serialVersionUID = 42L; // Serializable...
	

	private FileDesc() {}
	
	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (!(o instanceof FileDesc)) return false;
	    FileDesc fd = (FileDesc) o;
	    return Utils.areEquals(this.author, fd.author) &&
		    Utils.areEquals(this.title, fd.title) &&
		    Utils.areEquals(this.publisher, fd.publisher) &&
		    Utils.areEquals(this.section, fd.section) &&
		    Utils.areEquals(this.location, fd.location) &&
		    Utils.areEquals(this.magazine, fd.magazine) &&
		    Utils.areEquals(this.filename, fd.filename) &&
		    Utils.areEquals(this.filetype, fd.filetype) &&
		    Utils.areEquals(this.pages, fd.pages) &&
		    Utils.areEquals(this.creationtime, fd.creationtime);
	}
    }

    public class Public implements Serializable {
	public String publicId;
	public String uri;
	private static final long serialVersionUID = 42L; // Serializable...

	private Public() {
	}
	
	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (!(o instanceof Public)) return false;
	    Public pub = (Public) o;
	    return Utils.areEquals(this.publicId, pub.publicId) &&
		    Utils.areEquals(this.uri, pub.uri);
	}
    }

    public class LinguisticProcessor implements Serializable {
	String layer;
	String name;
	String timestamp;
	String beginTimestamp;
	String endTimestamp;
	String version;
	String hostname;
	private static final long serialVersionUID = 42L; // Serializable...

	private LinguisticProcessor(String name, String layer) {
	    this.layer = layer;
	    this.name = name;
	}

	/* Deprecated */
	private LinguisticProcessor(String name, String timestamp, String version) {
	    this.name = name;
	    this.timestamp = timestamp;
	    this.version = version;
	}

	public String getLayer() {
	    return this.layer;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public String getName() {
	    return name;
	}

	public boolean hasTimestamp() {
	    return this.timestamp != null;
	}

	public void setTimestamp(String timestamp) {
	    this.timestamp = timestamp;
	}

	public void setTimestamp() {
	    String timestamp = createTimestamp();
	    this.timestamp = timestamp;
	}

	public String getTimestamp() {
	    return this.timestamp;
	}

	public boolean hasBeginTimestamp() {
	    return beginTimestamp != null;
	}

	public void setBeginTimestamp(String timestamp) {
	    this.beginTimestamp = timestamp;
	    if (!this.hasHostname()) {
		try {
		    this.setHostname(InetAddress.getLocalHost().getHostName());
		} catch(UnknownHostException e) {}
	    }
	}

	public void setBeginTimestamp() {
	    String timestamp = createTimestamp();
	    this.setBeginTimestamp(timestamp);
	}

	public String getBeginTimestamp() {
	    return beginTimestamp;
	}

	public boolean hasEndTimestamp() {
	    return endTimestamp != null;
	}

	public void setEndTimestamp(String timestamp) {
	    this.endTimestamp = timestamp;
	}

	public void setEndTimestamp() {
	    String timestamp = createTimestamp();
	    this.endTimestamp = timestamp;
	}

	public String getEndTimestamp() {
	    return endTimestamp;
	}

	public boolean hasVersion() {
	    return version != null;
	}

	public void setVersion(String version) {
	    this.version = version;
	}

	public String getVersion() {
	    return version;
	}

	public Boolean hasHostname() {
	    return this.hostname != null;
	}

	public String getHostname() {
	    return this.hostname;
	}

	public void setHostname(String hostname) {
	    this.hostname = hostname;
	}

	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (!(o instanceof LinguisticProcessor)) return false;
	    LinguisticProcessor lp = (LinguisticProcessor) o;
	    return Utils.areEquals(this.layer, lp.layer) &&
		    Utils.areEquals(this.name, lp.name) &&
		    Utils.areEquals(this.version, lp.version);
	}
    }
    
}
