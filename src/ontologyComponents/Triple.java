package ontologyComponents;

public class Triple {

    Concept subject, object;
    Predicate predicate;

    public Triple(Concept subject, Predicate predicate, Concept object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    //--------Getter/Setter--------

    public Concept getSubject() {
        return subject;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public Concept getObject() {
        return object;
    }
}
