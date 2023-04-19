package com.heeexy.example.service;

import com.alibaba.fastjson.JSONObject;
import com.heeexy.example.dao.UserMapper;
import com.heeexy.example.util.CommonUtil;
import com.heeexy.example.util.constants.ErrorEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author: heeexy
 * @description: 用户/角色/权限
 * @date: 2017/11/2 10:18
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户列表
     */
    public JSONObject listUser(JSONObject jsonObject) {
        CommonUtil.fillPageParam(jsonObject);
        int count = userMapper.countUser(jsonObject);
        List<JSONObject> list = userMapper.listUser(jsonObject);
        return CommonUtil.successPage(jsonObject, list, count);
    }

    /**
     * 添加用户
     */
    public JSONObject addUser(JSONObject jsonObject) {
        int exist = userMapper.queryExistUsername(jsonObject);
        if (exist > 0) {
            return CommonUtil.errorJson(ErrorEnum.E_10009);
        }
        userMapper.addUser(jsonObject);
        userMapper.batchAddUserRole(jsonObject);
        return CommonUtil.successJson();
    }

    /**
     * 查询所有的角色
     * 在添加/修改用户的时候要使用此方法
     */
    public JSONObject getAllRoles() {
        List<JSONObject> roles = userMapper.getAllRoles();
        return CommonUtil.successPage(roles);
    }

    /**
     * 修改用户
     */
    public JSONObject updateUser(JSONObject jsonObject) {
        //不允许修改管理员信息
        if (jsonObject.getIntValue("userId") == 10001) return CommonUtil.successJson();
        userMapper.updateUser(jsonObject);
        userMapper.removeUserAllRole(jsonObject.getIntValue("userId"));
        if (!jsonObject.getJSONArray("roleIds").isEmpty()) {
            userMapper.batchAddUserRole(jsonObject);
        }
        return CommonUtil.successJson();
    }

    /**
     * 角色列表
     */
    public JSONObject listRole() {
        List<JSONObject> roles = userMapper.listRole();
        return CommonUtil.successPage(roles);
    }

    /**
     * 查询所有权限, 给角色分配权限时调用
     */

    public JSONObject listAllPermission() {
        List<JSONObject> permissions = userMapper.listAllPermission();
        return CommonUtil.successPage(permissions);
    }

    /**
     * 添加角色
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public JSONObject addRole(JSONObject jsonObject) {
        userMapper.insertRole(jsonObject);
        userMapper.insertRolePermission(jsonObject.getString("roleId"), (List<Integer>) jsonObject.get("permissions"));
        return CommonUtil.successJson();
    }

    /**
     * 修改角色
     */
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public JSONObject updateRole(JSONObject jsonObject) {
        String roleId = jsonObject.getString("roleId");
        List<Integer> newPerms = (List<Integer>) jsonObject.get("permissions");
        JSONObject roleInfo = userMapper.getRoleAllInfo(jsonObject);
        Set<Integer> oldPerms = (Set<Integer>) roleInfo.get("permissionIds");
        //修改角色名称
        updateRoleName(jsonObject, roleInfo);
        //添加新权限
        saveNewPermission(roleId, newPerms, oldPerms);
        //移除旧的不再拥有的权限
        removeOldPermission(roleId, newPerms, oldPerms);
        return CommonUtil.successJson();
    }

    /**
     * 修改角色名称
     */
    private void updateRoleName(JSONObject paramJson, JSONObject roleInfo) {
        String roleName = paramJson.getString("roleName");
        if (!roleName.equals(roleInfo.getString("roleName"))) {
            userMapper.updateRoleName(paramJson);
        }
    }

    /**
     * 为角色添加新权限
     */
    private void saveNewPermission(String roleId, Collection<Integer> newPerms, Collection<Integer> oldPerms) {
        List<Integer> waitInsert = new ArrayList<>();
        for (Integer newPerm : newPerms) {
            if (!oldPerms.contains(newPerm)) {
                waitInsert.add(newPerm);
            }
        }
        if (waitInsert.size() > 0) {
            userMapper.insertRolePermission(roleId, waitInsert);
        }
    }

    /**
     * 删除角色 旧的 不再拥有的权限
     */
    private void removeOldPermission(String roleId, Collection<Integer> newPerms, Collection<Integer> oldPerms) {
        List<Integer> waitRemove = new ArrayList<>();
        for (Integer oldPerm : oldPerms) {
            if (!newPerms.contains(oldPerm)) {
                waitRemove.add(oldPerm);
            }
        }
        if (waitRemove.size() > 0) {
            userMapper.removeOldPermission(roleId, waitRemove);
        }
    }

    /**
     * 删除角色
     */
    @Transactional(rollbackFor = Exception.class)
    public JSONObject deleteRole(JSONObject jsonObject) {
        String roleId = jsonObject.getString("roleId");
        int userCount = userMapper.countRoleUser(roleId);
        if (userCount > 0) {
            return CommonUtil.errorJson(ErrorEnum.E_10008);
        }
        userMapper.removeRole(roleId);
        userMapper.removeRoleAllPermission(roleId);
        return CommonUtil.successJson();
    }
}
