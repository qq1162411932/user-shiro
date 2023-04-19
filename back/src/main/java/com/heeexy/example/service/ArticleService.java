package com.heeexy.example.service;

import com.alibaba.fastjson.JSONObject;
import com.heeexy.example.dao.ArticleMapper;
import com.heeexy.example.util.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author: heeexy
 * @date: 2017/10/24 16:07
 */
@Service
public class ArticleService {

    @Autowired
    private ArticleMapper articleMapper;

    /**
     * 新增文章
     */
    @Transactional(rollbackFor = Exception.class)
    public JSONObject addArticle(JSONObject jsonObject) {
        articleMapper.addArticle(jsonObject);
        return CommonUtil.successJson();
    }

    /**
     * 文章列表
     */
    public JSONObject listArticle(JSONObject jsonObject) {
        CommonUtil.fillPageParam(jsonObject);
        int count = articleMapper.countArticle(jsonObject);
        List<JSONObject> list = articleMapper.listArticle(jsonObject);
        return CommonUtil.successPage(jsonObject, list, count);
    }

    /**
     * 更新文章
     */
    @Transactional(rollbackFor = Exception.class)
    public JSONObject updateArticle(JSONObject jsonObject) {
        articleMapper.updateArticle(jsonObject);
        return CommonUtil.successJson();
    }
}
