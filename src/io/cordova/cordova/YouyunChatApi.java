package io.cordova.cordova;

import matrix.sdk.message.ConvType;

/**
 * 游云接口API JS版
 * Created by 卫彪 on 2016/4/26.
 */
public interface YouyunChatApi {

    /**
     * 1、登录
     * @param callback
     */
    void login(ChatApiCallback callback);

    /**
     * 2、登出
     * @param callback
     */
    void logout(ChatApiCallback callback);

    /**
     * 3、发送文本
     * @param touchId uid or gid
     * @param text 文本内容
     * @param convType single or group
     * @param callback
     */
    void sendText(String touchId, String text, ConvType convType, ChatApiCallback callback);

    /**
     * 4、发送图片
     * @param touchId uid or gid
     * @param filePath 原图路径
     * @param thumbnailPath 缩略图路径
     * @param convType single or group
     * @param callback
     */
    void sendImage(String touchId, String filePath, String thumbnailPath, ConvType convType, ChatApiCallback callback);

    /**
     * 5、下载原图
     * @param fileId 文件ID
     * @param filePath 下载到路径
     * @param fileLength 文件长度
     * @param pieceSize 分配大小
     * @param callback
     */
    void downloadImage(String fileId, String filePath, int fileLength, int pieceSize, ChatApiCallback callback);

    /**
     * 6、创建群
     * @param callback
     */
    void createGroup(ChatApiCallback callback);

    /**
     * 7、退出群
     * @param groupId 群ID
     * @param callback
     */
    void exitGroup(String groupId, ChatApiCallback callback);

    /**
     * 8、添加用户
     * @param groupId 群ID
     * @param uids 用户IDs ["",""]
     * @param callback
     */
    void groupAddUser(String groupId, String uids, ChatApiCallback callback);

    /**
     * 9、删除用户
     * @param groupId 群ID
     * @param uids 用户IDs ["",""]
     * @param callback
     */
    void groupDeleteUser(String groupId, String uids, ChatApiCallback callback);

    /**
     * 10、查看群成员
     * @param groupId 群组ID
     * @param callback
     */
    void getUserListByGroupId(String groupId, ChatApiCallback callback);

    /**
     * 11、获取用户群列表
     * @param userId 用户ID
     * @param callback
     */
    void getGroupListByUserId(String userId, ChatApiCallback callback);

}
