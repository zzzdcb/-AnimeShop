package  com.dong.security;

import com.dong.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@NoArgsConstructor
public class CustomUserDetails implements UserDetails {

    private Long id; // 用户ID
    private String username; // 用户名
    private String password; // 密码
    private String nickname; // 昵称
    private String avatar; // 头像
    private String email; // 邮箱
    private Collection<? extends GrantedAuthority> authorities; // 权限

    public CustomUserDetails(User user) {
        // 将数据库中的角色转换为 Spring Security 的角色，this表示当前对象，即CustomUserDetails
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.nickname = user.getNickname();
        this.avatar = user.getAvatar();
        this.email = user.getEmail();

        // 数据库存单个数字：0=管理员, 1=用户
        Integer roleCode = user.getRole();  // 0 或 1
        String roleName = roleCode == 0 ? "ADMIN" : "USER";
        // 将数字角色转换为字符串角色
        this.authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + roleName)
        );
    }
}