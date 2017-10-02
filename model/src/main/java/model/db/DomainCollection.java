package model.db;

public enum DomainCollection {

    DOCS;

    public String collection(){
        return this.name().toLowerCase();
    }
}
