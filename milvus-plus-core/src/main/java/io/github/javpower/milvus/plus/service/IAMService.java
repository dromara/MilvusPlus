package io.github.javpower.milvus.plus.service;

import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.rbac.request.*;
import io.milvus.v2.service.rbac.response.DescribeRoleResp;
import io.milvus.v2.service.rbac.response.DescribeUserResp;

import java.util.List;

public interface IAMService {
     MilvusClientV2 getClient();

    /**
     * 创建的角色
     * @param roleName  要创建的角色的名称
     */
    default void createRole(String roleName){
        MilvusClientV2 client = getClient();
        client.createRole(CreateRoleReq.builder()
                .roleName(roleName)
                .build());
    }

    /**
     * 创建用户
     * @param userName 要创建的用户的名称。
     * @param password 要创建的用户的密码
     */
    default void createUser(String userName,String password){
        MilvusClientV2 client = getClient();
        client.createUser(CreateUserReq.builder()
                .userName(userName)
                .password(password)
                .build());
    }

    /**
     * 获取指定角色的权限信息。
     * <p>
     * 此方法通过调用Milvus客户端的{@code describeRole}函数来查询指定角色的权限详情。
     * 返回的{@code DescribeRoleResp}对象包含了一个{@code GrantInfo}列表，每个{@code GrantInfo}对象描述了角色被分配的单个权限。
     * </p>
     *
     * <p><b>详情:</b>
     * 每个{@code GrantInfo}对象包含以下信息：
     * <ul>
     *     <li>{@code objectType} - 被授予权限的对象类型。</li>
     *     <li>{@code permission} - 授予对象的特定权限。</li>
     *     <li>{@code objectName} - 被授予权限的对象名称。</li>
     *     <li>{@code dbName} - 与授予的权限关联的数据库名称。</li>
     *     <li>{@code grantor} - 授予权限的实体（用户或角色）的名称。</li>
     * </ul>
     * </p>
     *
     * <p><b>异常处理:</b>
     * 如果在操作期间发生错误，可能会抛出{@code MilvusClientExceptions}异常。调用者应适当处理此类异常。</p>
     * @param roleName 要查询其权限的角色名称。
     * @return 包含角色权限信息的{@code DescribeRoleResp}对象。
     */
    default DescribeRoleResp describeRole(String roleName) {
        MilvusClientV2 client = getClient();
        DescribeRoleResp describeRoleResp = client.describeRole(DescribeRoleReq.builder()
                .roleName(roleName)
                .build());
        return describeRoleResp;
    }

    /**
     * 获取指定用户的详细信息。
     * <p>
     * 此方法通过调用 Milvus 客户端的 API 来检索与特定用户名称相关联的角色和权限信息。
     * </p>
     *
     * @param userName 要查询的用户名称。
     * @return DescribeUserResp 包含用户详细信息的对象，其中包含与用户关联的角色列表。
     * @see DescribeUserResp
     */
    default DescribeUserResp describeUser(String userName) {
        // 获取 Milvus 客户端实例
        MilvusClientV2 client = getClient();
        // 构建请求并调用 describeUser 方法
        DescribeUserResp describeUserResp = client.describeUser(DescribeUserReq.builder()
                .userName(userName)
                .build());
        return describeUserResp;
    }
    /**
     * 删除指定的自定义角色。
     * <p>
     * 此方法通过调用 Milvus 客户端的 API 来删除一个自定义角色。
     * </p>
     *
     * @param roleName 要删除的角色名称。
     * @see DropRoleReq
     */
    default void dropRole(String roleName) {
        // 获取 Milvus 客户端实例
        MilvusClientV2 client = getClient();
        // 构建请求并调用 dropRole 方法
        client.dropRole(DropRoleReq.builder()
                .roleName(roleName)
                .build());
    }

    /**
     * 删除指定的用户。
     * <p>
     * 此方法通过调用 Milvus 客户端的 API 来删除一个用户。
     * </p>
     *
     * @param userName 要删除的用户名。
     * @see DropUserReq
     */
    default void dropUser(String userName) {
        // 获取 Milvus 客户端实例
        MilvusClientV2 client = getClient();
        // 构建请求并调用 dropUser 方法
        client.dropUser(DropUserReq.builder()
                .userName(userName)
                .build());
    }

    /**
     * 授予角色特定的权限。
     * <p>
     * 在基于角色的访问控制（RBAC）模型中，此方法用于将特定的权限授予给角色。
     * 角色可以是集合（Collection）、全局（Global）或用户（User）角色。
     * 权限必须先授予角色，然后用户通过该角色继承相应的权限。
     * </p>
     * <p>
     * 例如，如果想要授予一个角色对某个集合执行创建索引操作的权限，可以这样调用：
     * <pre>
     * grantPrivilege("roleName", "Collection", "CreateIndex", "collectionName");
     * </pre>
     * 对于全局权限，例如创建集合，可以省略对象名称或传递特定的 API 名称：
     * <pre>
     * grantPrivilege("adminRole", "Global", "CreateCollection", "");
     * </pre>
     * </p>
     * <p>
     * 权限名称和对象名称是区分大小写的。要授予对某种类型对象的所有操作权限，可以使用通配符 "*"。
     * 例如，授予一个角色对所有集合的所有操作权限：
     * <pre>
     * grantPrivilege("adminRole", "Collection", "*", "");
     * </pre>
     * </p>
     *
     * @param roleName 要授予权限的角色名称。
     * @param objectType 权限对象的类型，例如 "Collection", "Global", 或 "User"。
     * @param privilege 要授予的权限名称，例如 "CreateIndex", "DropCollection", 或通配符 "*"。
     * @param objectName 权限适用的对象名称，对于集合类型对象，应提供集合名称；对于全局操作，可以为空或指定 API。
     */
    default void grantPrivilege(String roleName, String objectType, String privilege, String objectName) {
        MilvusClientV2 client = getClient(); // 获取 Milvus 客户端实例
        client.grantPrivilege(GrantPrivilegeReq.builder()
                .roleName(roleName)
                .objectType(objectType)
                .privilege(privilege)
                .objectName(objectName)
                .build());
    }
    /**
     * 授予用户特定角色。
     * <p>
     * 在基于角色的访问控制（RBAC）模型中，此方法用于将一个角色授予给一个已存在的用户。
     * 用户通过被授予的角色继承相应的权限。
     * </p>
     *
     * @param roleName 要授予给用户的角色名称。
     * @param userName 已存在的用户名称。
     * @see MilvusClientV2#grantRole(GrantRoleReq)
     */
    default void grantRole(String roleName, String userName) {
        MilvusClientV2 client = getClient(); // 获取 Milvus 客户端实例
        client.grantRole(GrantRoleReq.builder()
                .roleName(roleName)
                .userName(userName)
                .build());
    }

    /**
     * 列出所有自定义角色。
     * <p>
     * 此方法调用 Milvus 客户端的 API，以获取系统中定义的所有自定义角色的名称列表。
     * </p>
     *
     * @return 包含角色名称的字符串列表。
     * @see MilvusClientV2#listRoles()
     */
    default List<String> listRoles() {
        MilvusClientV2 client = getClient(); // 获取 Milvus 客户端实例
        return client.listRoles(); // 调用 listRoles 方法并返回角色名称列表
    }

    /**
     * 列出所有现有用户的用户名。
     * <p>
     * 此方法调用 Milvus 客户端的 API，以获取系统中所有现有用户的用户名列表。
     * </p>
     *
     * @return 包含用户名的字符串列表。
     * @see MilvusClientV2#listUsers()
     */
    default List<String> listUsers() {
        MilvusClientV2 client = getClient(); // 获取 Milvus 客户端实例
        return client.listUsers(); // 调用 listUsers 方法并返回用户名列表
    }

    /**
     * 撤销已分配给角色的权限。
     * <p>
     * 在基于角色的访问控制（RBAC）模型中，此方法用于撤销先前分配给角色的特定权限。
     * 可选的数据库名称参数可以限制在指定数据库中撤销权限。
     * </p>
     *
     * @param roleName 要从中撤销权限的角色名称。
     * @param objectType 权限对象的类型。可能的值包括 "Global", "Collection", 和 "User"。
     * @param privilege 要撤销的权限名称。具体权限详情请参阅用户和角色表中的 "Privilege name" 列。
     * @param objectName 要撤销权限的 API 名称。可以使用通配符 "*" 来表示所有适用的 API。
     * @param databaseName 可选的数据库名称，用于限制权限撤销的范围在指定数据库内。
     * @see MilvusClientV2#revokePrivilege(RevokePrivilegeReq)
     */
    default void revokePrivilege(String roleName, String objectType, String privilege, String objectName, String databaseName) {
        MilvusClientV2 client = getClient(); // 获取 Milvus 客户端实例
        client.revokePrivilege(RevokePrivilegeReq.builder()
                .roleName(roleName)
                .objectType(objectType)
                .privilege(privilege)
                .objectName(objectName)
                .dbName(databaseName)
                .build());
    }
    /**
     * 撤销用户的角色。
     * <p>
     * 在基于角色的访问控制（RBAC）模型中，此方法用于撤销之前授予用户的特定角色。
     * 用户将失去通过该角色获得的所有权限。
     * </p>
     *
     * @param roleName 要撤销的角色名称。
     * @param userName 现有用户的用户名。
     * @see MilvusClientV2#revokeRole(RevokeRoleReq)
     */
    default void revokeRole(String roleName, String userName) {
        MilvusClientV2 client = getClient(); // 获取 Milvus 客户端实例
        client.revokeRole(RevokeRoleReq.builder()
                .roleName(roleName)
                .userName(userName)
                .build());
    }
    /**
     * 更新指定用户的密码。
     * <p>
     * 此方法用于更改系统中现有用户的密码。
     * </p>
     *
     * @param userName 现有用户的用户名。
     * @param password 用户当前的密码。
     * @param newPassword 用户的新密码。
     * @see MilvusClientV2#updatePassword(UpdatePasswordReq)
     */
    default void updatePassword(String userName, String password, String newPassword) {
        MilvusClientV2 client = getClient(); // 获取 Milvus 客户端实例
        client.updatePassword(UpdatePasswordReq.builder()
                .userName(userName)
                .password(password)
                .newPassword(newPassword)
                .build());
    }

}
