package cc.feitwnd.context;

public final class BaseContext {

    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();
    private static final ThreadLocal<Integer> CURRENT_ROLE = new ThreadLocal<>();

    private BaseContext() {
    }

    public static void setCurrentId(Long id) {
        CURRENT_ID.set(id);
    }

    public static Long getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void removeCurrentId() {
        CURRENT_ID.remove();
    }

    public static void setCurrentRole(Integer role) {
        CURRENT_ROLE.set(role);
    }

    public static Integer getCurrentRole() {
        return CURRENT_ROLE.get();
    }

    public static void removeCurrentRole() {
        CURRENT_ROLE.remove();
    }

}
