package MacroModel.osm.core;

public class RelationMember<T> {

    private T member;
    private String role;

    public RelationMember(T member, String role) {
        this.member = member;
        this.role = role;
    }

    public T getMember() {
        return member;
    }

    public String getRole() {
        return role;
    }
}
