package top.imyzt.learning.dp.proxy.a_static;

/**
 * @author imyzt
 * @date 2019/5/28
 * @description 目标对象
 */
public class UserDao implements IUserDao {

    public void save() {
        System.out.println("保存用户");
    }

    public void queryUserById(int id) {
        System.out.printf("id=%d, 用户详情", id);
    }
}
