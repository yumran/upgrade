package com.yscz.upgrade.controller;

import com.yscz.upgrade.bean.RespBean;
import com.yscz.upgrade.config.ViewConfig;
import com.yscz.upgrade.service.UpgradeService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;


@RestController
public class UpgradeController {

    private static Logger logger = LoggerFactory.getLogger(UpgradeController.class);

    @Autowired
    private UpgradeService upgradeService;

    /**
     * 本地升级 指定升级包路径+升级包名
     * @param localXmlPath
     * @param upgradePKG
     * @return
     */
    @RequestMapping(value = "localUpgrade", method = RequestMethod.POST)
    @ResponseBody
    public RespBean localUpgrade(@RequestParam(value = "localXmlPath") String localXmlPath, @RequestParam(value = "upgradePKG") String upgradePKG) {
        logger.info("UpgradeController localUpgrade localXmlPath:" + localXmlPath + ", upgradePKG:" + upgradePKG);
        if(StringUtils.isEmpty(localXmlPath) || StringUtils.isEmpty(upgradePKG)) {
            return RespBean.error("本地版本记录文件或升级包文件不可为空， 请指定本地版本记录文件或升级包文件路径！！");
        }

        if(ViewConfig.OSName.contains("win")) {
            String upgradeBasicPath = upgradePKG.substring(0, upgradePKG.lastIndexOf("\\") + 1);    //升级包去除文件名的路径 (基础路径)
            String upgradeXmlPath = upgradePKG.substring(0, upgradePKG.lastIndexOf(".zip")) + "\\upgrade.xml";
            return upgradeService.upgradeAplication_win(upgradeBasicPath, upgradePKG, localXmlPath, upgradeXmlPath);
        }else if(ViewConfig.OSName.contains("linux")){
            String upgradeBasicPath = upgradePKG.substring(0, upgradePKG.lastIndexOf("/") + 1);    //升级包去除文件名的路径 (基础路径)
            String upgradeXmlPath = upgradePKG.substring(0, upgradePKG.lastIndexOf(".zip")) + "/upgrade.xml";
            return upgradeService.upgradeAplication_linux(upgradeBasicPath, upgradePKG, localXmlPath, upgradeXmlPath);
        }
        return RespBean.ok();
    }
}
