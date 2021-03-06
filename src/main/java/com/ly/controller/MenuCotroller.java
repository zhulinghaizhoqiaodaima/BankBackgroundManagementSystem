package com.ly.controller;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.ly.model.Gworder;
import com.ly.model.Menu;
import com.ly.model.User;
import com.ly.model.Worder;
import com.ly.service.MenuService;
import com.ly.service.UserService;
import com.ly.service.WorderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.Model;

import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/menu")
public class MenuCotroller {
    @Autowired
    private MenuService menuService;
    @Autowired
    private WorderService worderService;
    @Autowired
    private UserService userService;

    public static final int PAGE_SIZE=10;

    @RequestMapping("/list")
    public ModelAndView list(){
        ModelAndView modelAndView=new ModelAndView();
        List<Menu> menuList= menuService.list();
        modelAndView.addObject("menuList",menuList);
        modelAndView.setViewName("week-menu");
        return modelAndView;
    }

    @RequestMapping(value = "/addmenuy")
    public String addmenuy(Model model){
        Calendar cal = Calendar.getInstance();
        Date date=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String etime=sdf.format(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;
        int week= cal.get(Calendar.WEEK_OF_MONTH);
        int weekday = cal.get(Calendar.DAY_OF_WEEK)-1;
        if(weekday==0){
            int week1=week-1;
            model.addAttribute("year",year);
            model.addAttribute("month",month);
            model.addAttribute("week",week1);
            return "menu-add";
        }
        model.addAttribute("year",year);
        model.addAttribute("month",month);
        model.addAttribute("week",week);
        return "menu-add";
    }

    @RequestMapping(value="/addmenu",method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> addmenu(@RequestParam("year") int year,@RequestParam("month") int month, @RequestParam("week") int week, @RequestParam("weekday") int weekday,
                                       @RequestParam("meatone") String meatone, @RequestParam("meattwo") String meattwo,
                                       @RequestParam("vegetable") String vegetable, @RequestParam("soup") String soup, HttpSession httpSession
                                       ) {
        Menu menu=new Menu();
        Date date=new Date();
        SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String time=dateFormat.format(date);
        menu.setYear(year);
        menu.setMonth(month);
        menu.setWeek(week);
        menu.setWeekday(weekday);
        menu.setMeatone(meatone);
        menu.setMeattwo(meattwo);
        menu.setVegetable(vegetable);
        menu.setSoup(soup);
        menu.setDate(time);
        Menu fmenu=menuService.PdMenu(year,month,week,weekday);
        int fcmenu=menuService.FcMenu();
        Map<String, Object> map = new HashMap<>();
        if (fmenu != null) {
            map.put("result", false);
            map.put("message", "???????????????");
            return map;
        }
        if(fcmenu <7) {
            if(fcmenu==0){
                Worder worder=new Worder();
                //????????????????????????set??????
                String wtime = dateFormat.format(date);
                worder.setWtime(wtime);
                //??????session????????????????????????String??????
                Object name=httpSession.getAttribute("uname");
                String username=(String)name;
                User user=userService.selectUserByUserName(username);
                String rname=user.getName();
                worder.setName(rname);
                worder.setYear(year);
                worder.setMonth(month);
                worder.setWeek(week);
                //????????????????????????????????????
                worderService.intowOrdert(worder);
                //?????????????????????????????????
                menuService.insertweekmenu(menu);
                //???????????????????????????id?????????????????????????????????????????????????????????
                int wOid=worderService.findwOid();
                int mid=worderService.findMidByWeekday(weekday);
                Gworder gworder=new Gworder();
                gworder.setwOid(wOid);
                gworder.setMid(mid);
                gworder.setMeatone(meatone);
                gworder.setMeattwo(meattwo);
                gworder.setVegetable(vegetable);
                gworder.setSoup(soup);
                gworder.setYear(year);
                gworder.setMonth(month);
                gworder.setWeek(week);
                gworder.setWeekday(weekday);
                boolean flag=worderService.intoGwordert(gworder);
                System.out.println(flag);
                map.put("result", flag);
                map.put("message", flag ? "????????????" : "????????????");
                return map;
            }
            if(fcmenu!=0){
                //?????????????????????????????????
                menuService.insertweekmenu(menu);
                //???????????????????????????id?????????????????????????????????????????????????????????
                int wOid=worderService.findwOid();
                int mid=worderService.findMidByWeekday(weekday);
                Gworder gworder=new Gworder();
                gworder.setwOid(wOid);
                gworder.setMid(mid);
                gworder.setMeatone(meatone);
                gworder.setMeattwo(meattwo);
                gworder.setVegetable(vegetable);
                gworder.setSoup(soup);
                gworder.setYear(year);
                gworder.setMonth(month);
                gworder.setWeek(week);
                gworder.setWeekday(weekday);
                boolean flag=worderService.intoGwordert(gworder);
                System.out.println(flag);
                map.put("result", flag);
                map.put("message", flag ? "????????????" : "????????????");
                return map;
            }
        }
        map.put("result", false);
        map.put("message", "??????????????????");
        return map;
    }

    @RequestMapping("/updatmenu")
    public String updatemenu(Integer mid, Model model){
        Menu menu=menuService.selectMenuByMid(mid);
        model.addAttribute("menu",menu);
        return "menu-update";
    }

    @RequestMapping("/doupdate")
    public String doupdate(Menu menu){
        menuService.updateMenuByMid(menu);
        return "redirect:/menu/list";
    }

    @RequestMapping("/del/{mid}")
    public String del(@PathVariable("mid") int mid){
        menuService.del(mid);
        return "redirect:/menu/list";
    }

    @RequestMapping("/end")
    public String end(){
        //??????????????????
        menuService.clear();
        return "redirect:/menu/list";
    }

    @RequestMapping("/oldmenu")
    public ModelAndView oldmenu(@RequestParam(value="pageNum",required=false,defaultValue="1") int pageNum){
        ModelAndView modelAndView=new ModelAndView();
        PageHelper.startPage(pageNum,PAGE_SIZE);
        List<Worder> worderList= menuService.worderlist();
        PageInfo pageInfo=new PageInfo<>(worderList);
        modelAndView.addObject("wordertList",worderList);
        modelAndView.addObject("page",pageInfo);
        modelAndView.setViewName("old-menu");
        return modelAndView;
    }

    @RequestMapping("/Coldmenu")
    public ModelAndView Coldmenu(@RequestParam(value="pageNum",required=false,defaultValue="1") int pageNum,
                                 @RequestParam("year") Integer year,@RequestParam("month") Integer month){
        ModelAndView modelAndView=new ModelAndView();
        PageHelper.startPage(pageNum,PAGE_SIZE);
        List<Worder> worderList= menuService.Cworderlist(year,month);
        PageInfo pageInfo=new PageInfo<>(worderList);
        modelAndView.addObject("wordertList",worderList);
        modelAndView.addObject("page",pageInfo);
        modelAndView.setViewName("old-menu");
        return modelAndView;
    }

    @RequestMapping("/mordert")
    public String mordert(int wOid, Model model){
        List<Gworder> gworderList=worderService.findmordertBywOid(wOid);
        model.addAttribute("gworderList",gworderList);
        return "oldmenu-message";
    }

    @RequestMapping("/admlist")
    public ModelAndView admlist(){
        ModelAndView modelAndView=new ModelAndView();
        List<Menu> menuList= menuService.list();
        modelAndView.addObject("menuList",menuList);
        modelAndView.setViewName("admweek-menu");
        return modelAndView;
    }
}
