var _navigations = {"navigations":[
    
]};
/**
 * 初始化页面导航
 * @param navigation_id 导航ID，具体查看上面json
 * @param type 0：当前页面；1：父级目录。
 */
function initPageNavigation(navigation_id,type){
    $.each(_navigations.navigations, function(i, n) {
        if(navigation_id == n.id){
            var html = "";
            if(type == "0"){
                html = '<li class="active">'+ n.name+'</li>';
            }else if(type == "1"){
                if(n.parent == "-1"){
                    html = '<li><a href="'+ basePath + n.url +'" name="navigation"><i class="fa fa-dashboard"></i> '+ n.name+'</a></li>';
                }else{
                    html = '<li><a href="'+ basePath + n.url +'" name="navigation">'+ n.name+'</a></li>';
                }
            }
            $(".breadcrumb").prepend(html);
            // 不是更目录，则递归输出导航
            if(n.parent != "-1"){
                initPageNavigation(n.parent,"1");
            }
        }
    });
}