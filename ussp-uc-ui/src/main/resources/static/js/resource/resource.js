layui.use(['element', 'form', 'ztree', 'table'], function () {
    var $ = layui.jquery
        , element = layui.element
        , form = layui.form
        , table = layui.table
        , addDialog = 0 //新增弹出框的ID
        , viewDialog = 0 //查询弹出框的ID
        , editDialog = 0 //修改弹出框的ID
        , menuTableLoad = false//菜单数据表格是否已加载
        , btnTableLoad = false//按钮数据表格是否已加载
        , tabTableLoad = false//tab数据表格是否已加载
        , apiTableLoad = false//api数据表格是否已加载
        , moduleTableLoad = false//module数据表格是否已加载
        , selectBottomTabIndex = 0//当前选中的tab标签页
        , appAndResourceTree //组织机构树控件
        , active = {
        add: function (type) { //弹出用户新增弹出框
            var nodes = appAndResourceTree.getSelectedNodes();
            var checkStatus = table.checkStatus('resource_menu_datatable');
            layer.close(addDialog);
            var title;
            switch (type) {
                case "menu":
                    title = "新增菜单";
                    break;
                case "btn":
                    title = "新增按钮权限";
                    break;
                case "tab":
                    title = "新增TAB权限";
                    break;
                case "api":
                    title = "新增API权限";
                    break;
                case "module":
                    title = "新增模块";
                    break;
            }
            switch (type) {
                case "module":
                case "menu":
                    if (nodes.length == 0) {
                        layer.alert("请先选择一个父菜单或系统。");
                        return false;
                    } else if (nodes[0]["type"] == "view") {
                        layer.alert("您选择的菜单【" + nodes[0]["name"] + "】不能增加下级菜单，请重新选择。");
                        return false;
                    }
                    break;
                case "api":
                case "tab":
                case "btn":
                    if (!checkStatus || !checkStatus.data || checkStatus.data.length == 0) {
                        layer.alert("请先在上面的表格中选择一个父菜单。");
                        return false;
                    }
                    break;
            }
            addDialog = layer.open({
                type: 1,
                area: ['400px', '450px'],
                shadeClose: true,
                title: title,
                content: $("#resource_" + type + "_add_data_div").html(),
                btn: ['保存', '取消'],
                yes: function (index, layero) {
                    var $submitBtn = $("button[lay-filter=resource_" + type + "_add_data_form]", layero);
                    if ($submitBtn) {
                        $submitBtn.click();
                    } else {
                        throw new Error("没有找到submit按钮。");
                    }
                },
                btn2: function () {
                    layer.closeAll('tips');
                },
                success: function (layero, index) {
                    var app = nodes[0]["app"];
                    var appName = appAndResourceTree.getNodesByParam("code", app)[0]["name"];
                    //初始弹出框表单数据
                    switch (type) {
                        case "menu":
                            if (nodes[0]["type"] == "group") {
                                $("input[name=resParent]", layero).val(nodes[0]["code"]);
                                $("input[name=resParentName]", layero).val(nodes[0]["name"]);
                            }
                        case "module":
                            if (nodes[0]["type"] == "module") {
                                $("input[name=resParent]", layero).val(nodes[0]["code"]);
                                $("input[name=resParentName]", layero).val(nodes[0]["name"]);
                            }
                            $("input[name=app]", layero).val(app);
                            $("input[name=appName]", layero).val(appName);
                            break;
                        case "api":
                        case "tab":
                        case "btn":
                            $("input[name=app]", layero).val(app);
                            $("input[name=appName]", layero).val(appName);
                            var selectData = checkStatus.data[0];
                            $("input[name=resParent]", layero).val(selectData["resCode"]);
                            $("input[name=resParentName]", layero).val(selectData["resNameCn"]);
                            break;
                    }
                    form.render(null, "resource_" + type + "_add_data_form");
                    form.on("submit(resource_" + type + "_add_data_form)", function (data) {
                        data.field.resType = type;
                        $.ajax({
                            type: "POST",
                            url: "http://localhost:9999/resource/add",
                            data: JSON.stringify(data.field),
                            contentType: "application/json; charset=utf-8",
                            success: function (result) {
                                layer.close(index);
                                if (result["returnCode"] == '0000') {
                                    if (type == 'module') {
                                        moduleTableLoad = false;
                                    } else {
                                        menuTableLoad = false;
                                        btnTableLoad = false;
                                        tabTableLoad = false;
                                        apiTableLoad = false;
                                    }
                                    renderTable(type);
                                    layer.alert(title + "成功。");
                                }
                            },
                            error: function (result) {
                                layer.msg(title + "发生异常，请联系管理员。");
                                console.error(result);
                            }
                        });
                        return false;
                    });
                }
            })
        },
        search: function (type) {
            var selectNodes = appAndResourceTree.getSelectedNodes();
            if (selectNodes && selectNodes.length == 1) {
                var keyword = $("#resource_" + type + "_search_keyword").val();
                var resType = selectNodes[0]["type"];
                if (type == 'module') {
                    moduleTableLoad = false;
                } else {
                    menuTableLoad = false;
                    btnTableLoad = false;
                    tabTableLoad = false;
                    apiTableLoad = false;
                }
                if (resType != "view" && resType != "group" && resType != "module") {
                    renderTable(type, null, keyword);
                } else {
                    renderTable(type, undefined, keyword);
                }
            }
        }
    };
    //渲染组织机构树
    appAndResourceTree = $.fn.zTree.init($('#resource_app_auth_ztree_left'), {
            async: {
                enable: true,
                url: basepath + "/resource/app/load",
                dataFilter: function (treeId, parentNode, childNodes) {
                    if (!childNodes) return null;
                    for (var i = 0, l = childNodes.length; i < l; i++) {
                        //childNodes[i].open = true;
                        childNodes[i].name = childNodes[i]["nameCn"].replace(/\.n/g, '.');
                    }
                    return childNodes;
                }
            }
            , view: {
                showIcon: false
                , selectedMulti: false
                , fontCss: function (treeId, treeNode) {
                    return (!!treeNode.highlight) ? {color: "#A60000", "font-weight": "bold"} : {
                        color: "#333",
                        "font-weight": "normal"
                    };
                }
            }
            , callback: {
                onClick: function (event, treeId, treeNode) {
                    var resType = treeNode["type"];
                    switch (resType) {
                        case "view":
                        case "group":
                        case "app":
                        case "menuType":
                            menuTableLoad = false;
                            btnTableLoad = false;
                            tabTableLoad = false;
                            apiTableLoad = false;
                            if (resType != "view" && resType != "group") {
                                renderTable('menu', null);
                            } else {
                                renderTable('menu');
                            }
                            renderTable('btn', treeNode["code"]);
                            element.tabChange("resource_top_tab", "resMenuType");
                            element.tabChange("resource_bottom_tab", "resBtnType");
                            break;
                        case "moduleType":
                        case "module":
                            moduleTableLoad = false;
                            if (resType != "module") {
                                renderTable('module', null);
                            } else {
                                renderTable('module');
                            }
                            element.tabChange("resource_top_tab", "resModuleType");
                            break;
                    }
                },
                onAsyncSuccess: function (event, treeId, treeNode) {
                    var node = appAndResourceTree.getNodeByParam("level ", "0");
                    if (node) {
                        appAndResourceTree.selectNode(node);
                    }
                }
            },
            data: {
                simpleData: {
                    enable: true
                    , idKey: "code"
                    , pIdKey: "parentCode"
                }
            }
        }
    );
    //渲染用户数据表格
    renderTable("menu");
    renderTable("btn");

    /**
     * 刷新数据表
     * @param type 表格标识类型
     * @param parentCode 过滤的父资源编码（为空，则获取左边树选中的资源编码）
     * @param keyword 过滤的关键字
     */
    function renderTable(type, parentCode, keyword) {
        var app = null, resType = null;
        if (!keyword) {
            keyword = null;
        }
        switch (type) {
            case "module":
            case "menu":
                var selectNodes = appAndResourceTree.getSelectedNodes();
                if (selectNodes && selectNodes.length == 1) {
                    app = selectNodes[0]["app"];
                    if (typeof(parentCode) == 'undefined' && parentCode == null) {
                        parentCode = selectNodes[0]["code"];
                    }
                }
                break;
            case "api":
            case "tab":
            case "btn":
                var checkStatus = table.checkStatus('resource_menu_datatable');
                if (checkStatus && checkStatus.data.length == 1) {
                    if (!parentCode) {
                        parentCode = checkStatus.data[0]["resCode"];
                    }
                    app = checkStatus.data[0]["app"];
                }
                break;
        }
        var clos = [[]], height = 'full', page = false, limit = 999, limits = [],
            initSort = {field: 'lastModifiedDatetime', type: 'desc'};
        switch (type) {
            case 'menu':
                if (menuTableLoad) {
                    return false;
                }
                menuTableLoad = true;
                resType = "group,view";
                height = '276';
                page = true;
                limit = 5;
                limits = [5, 10, 20, 30, 40, 50];
                initSort = {field: 'sequence', type: 'asc'};
                clos = [[
                    {type: 'numbers'}
                    , {type: 'checkbox'}
                    , {field: 'resCode', width: 120, title: '菜单编号'}
                    , {field: 'resNameCn', width: 150, title: '菜单名称'}
                    , {field: 'resContent', title: '菜单链接'}
                    , {field: 'fontIcon', width: 60, title: '图标'}
                    , {field: 'sequence', width: 60, title: '顺序'}
                    , {field: 'resParent', width: 120, title: '父菜单编号'}
                    , {field: 'status', width: 60, title: '状态', templet: "#resource_table_status_laytpl"}
                    , {field: 'updateOperator', width: 100, title: '更新人'}
                    , {field: 'lastModifiedDatetime', width: 150, title: '更新时间'}
                    , {width: 178, title: '操作', align: 'center', toolbar: '#resource_table_btn'}
                ]];
                break;
            case 'btn':
                if (btnTableLoad) {
                    return false;
                }
                btnTableLoad = true;
                resType = 'btn';
                height = 'full-601';
                initSort = {field: 'sequence', type: 'asc'};
                clos = [[
                    {type: 'numbers'}
                    , {field: 'resCode', width: 120, title: '按钮编号'}
                    , {field: 'resNameCn', title: '按钮名称'}
                    , {field: 'fontIcon', width: 60, title: '图标'}
                    , {field: 'sequence', width: 60, title: '顺序'}
                    , {field: 'resParent', width: 120, title: '父菜单编号'}
                    , {field: 'status', width: 60, title: '状态', templet: "#resource_table_status_laytpl"}
                    , {field: 'updateOperator', width: 100, title: '更新人'}
                    , {field: 'lastModifiedDatetime', width: 150, title: '更新时间'}
                    , {width: 178, title: '操作', align: 'center', toolbar: '#resource_table_btn'}
                ]];
                break;
            case 'tab':
                if (tabTableLoad) {
                    return false;
                }
                tabTableLoad = true;
                resType = 'tab';
                height = 'full-601';
                initSort = {field: 'sequence', type: 'asc'};
                clos = [[
                    {type: 'numbers'}
                    , {field: 'resCode', width: 120, title: 'TAB编号'}
                    , {field: 'resNameCn', width: 150, title: 'TAB名称'}
                    , {field: 'resContent', title: 'TAB链接'}
                    , {field: 'sequence', width: 60, title: '顺序'}
                    , {field: 'resParent', width: 120, title: '父菜单编号'}
                    , {field: 'status', width: 60, title: '状态', templet: "#resource_table_status_laytpl"}
                    , {field: 'updateOperator', width: 100, title: '更新人'}
                    , {field: 'lastModifiedDatetime', width: 150, title: '更新时间'}
                    , {width: 178, title: '操作', align: 'center', toolbar: '#resource_table_btn'}
                ]];
                break;
            case 'api':
                if (apiTableLoad) {
                    return false;
                }
                apiTableLoad = true;
                resType = 'api';
                height = 'full-601';
                clos = [[
                    {type: 'numbers'}
                    , {field: 'resCode', width: 120, title: 'API编号'}
                    , {field: 'resNameCn', width: 150, title: 'API名称'}
                    , {field: 'resContent', title: 'API链接'}
                    , {field: 'remark', width: 200, title: '方法名'}
                    , {field: 'resParent', width: 120, title: '父菜单编号'}
                    , {field: 'status', width: 60, title: '状态', templet: "#resource_table_status_laytpl"}
                    , {field: 'updateOperator', width: 100, title: '更新人'}
                    , {field: 'lastModifiedDatetime', width: 150, title: '更新时间'}
                    , {width: 178, title: '操作', align: 'center', toolbar: '#resource_table_btn'}
                ]];
                break;
            case 'module':
                if (moduleTableLoad) {
                    return false;
                }
                moduleTableLoad = true;
                resType = 'module';
                height = 'full-234';
                page = true;
                limit = 20;
                limits = [20, 30, 40, 50, 60, 70];
                initSort = {field: 'sequence', type: 'asc'};
                clos = [[
                    {type: 'numbers'}
                    , {field: 'resCode', width: 120, title: '模块编号'}
                    , {field: 'resNameCn', width: 150, title: '模块名称'}
                    , {field: 'resContent', title: '模块链接'}
                    , {field: 'fontIcon', width: 60, title: '图标'}
                    , {field: 'sequence', width: 60, title: '顺序'}
                    , {field: 'resParent', width: 120, title: '父模块编号'}
                    , {field: 'status', width: 60, title: '状态', templet: "#resource_table_status_laytpl"}
                    , {field: 'updateOperator', width: 100, title: '更新人'}
                    , {field: 'lastModifiedDatetime', width: 150, title: '更新时间'}
                    , {width: 178, title: '操作', align: 'center', toolbar: '#resource_table_btn'}
                ]];
                break;
        }
        table.render({
            id: 'resource_' + type + '_datatable'
            , elem: '#resource_' + type + '_datatable'
            , url: basepath + 'resource/page/load.json'
            , where: {
                app: app,
                resType: resType,
                parentCode: parentCode,
                keyWord: keyword
            }
            , initSort: initSort
            , page: page
            , limit: 9999
            , limits: limits
            , height: height
            , cols: clos
        });
    }

    //监听操作栏
    table.on('tool(resource_menu_datatable)', function (obj) {
            var data = obj.data;
            if (obj.event === 'detail') {
                $.post("http://localhost:9999/user/view/" + data.userId, null, function (result) {
                    if (result["returnCode"] == "0000") {
                        viewDialog = layer.open({
                            type: 1,
                            area: ['680px', '360px'],
                            shadeClose: true,
                            title: "修改用户",
                            content: $("#user_view_data_div").html(),
                            btn: ['取消'],
                            btn2: function () {
                                layer.closeAll('tips');
                            },
                            success: function (layero) {
                                var status = result.data.status;
                                result.data.status = status === "0" ? "正常" : (status === "1" ? "禁用" : (status === "4" ? "冻结" : (status === "1" ? "锁定" : result.data.status)));
                                $.each(result.data, function (name, value) {
                                    var $input = $("input[name=" + name + "]", layero);
                                    if ($input && $input.length == 1) {
                                        $input.val(value);
                                    }
                                });
                            }
                        })
                    } else {
                        layer.msg(result.codeDesc);
                    }
                });
            } else if (obj.event === 'del') {
                layer.confirm('是否删除用户' + data.userName + "？", function (index) {
                    $.post("http://localhost:9999/user/delete/" + data.userId, null, function (result) {
                        if (result["returnCode"] == "0000") {
                            refreshTable();
                            layer.close(index);
                            layer.msg("删除用户成功。");
                        } else {
                            layer.msg(result.codeDesc);
                        }
                    });
                });
            } else if (obj.event === 'edit') {
                layer.close(editDialog);
                $.post("http://localhost:9999/user/view/" + data.userId, null, function (result) {
                    if (result["returnCode"] == "0000") {
                        editDialog = layer.open({
                            type: 1,
                            area: ['400px', '380px'],
                            shadeClose: true,
                            title: "修改用户",
                            content: $("#user_modify_data_div").html(),
                            btn: ['保存', '取消'],
                            yes: function (index, layero) {
                                var $submitBtn = $("button[lay-filter=user_filter_modify_data_form]", layero);
                                if ($submitBtn) {
                                    $submitBtn.click();
                                } else {
                                    throw new Error("没有找到submit按钮。");
                                }
                            },
                            btn2: function () {
                                layer.closeAll('tips');
                            },
                            success: function (layero, index) {
                                //表单数据填充
                                $.each(result.data, function (name, value) {
                                    var $input = $("input[name=" + name + "]", layero);
                                    if ($input && $input.length == 1) {
                                        $input.val(value);
                                    }
                                });
                                form.render(null, "user_filter_modify_data_form");
                                form.on('submit(user_filter_modify_data_form)', function (data) {
                                    $.ajax({
                                        type: "POST",
                                        url: "http://localhost:9999/user/update",
                                        data: JSON.stringify(data.field),
                                        contentType: "application/json; charset=utf-8",
                                        success: function (result2) {
                                            layer.close(index);
                                            if (result2["returnCode"] == '0000') {
                                                refreshTable();
                                                layer.alert("用户修改成功。");
                                            }
                                        },
                                        error: function (message) {
                                            layer.msg("用户新增发生异常，请联系管理员。");
                                            layer.close(index);
                                            console.error(message);
                                        }
                                    });
                                    return false;
                                });
                            }
                        })
                    } else {
                        layer.msg(result.codeDesc);
                    }
                });
            }
        }
    );
    //菜单和模块tab页切换事件
    element.on('tab(resource_top_tab)', function (data) {
        switch (data.index) {
            case 0:
                element.tabChange("resource_bottom_tab", "resBtnType");
                break;
            case 1:
                renderTable("module");
                break;
        }
    });
    //按钮权限/TAB权限/API权限tab页切换事件
    element.on('tab(resource_bottom_tab)', function (data) {
        selectBottomTabIndex = data.index;
        switch (data.index) {
            case 1:
                renderTable("tab");
                break;
            case 2:
                renderTable("api");
                break;
        }
    });
    //监控菜单datatable行选中事件
    table.on("rowClick(resource_menu_datatable)", function (data) {
        data.checkbox.click();
    });
    //监控菜单datatable复选框点击事件
    table.on('checkbox(resource_menu_datatable)', function (data) { //注：edit是固定事件名，test是table原始容器的属性 lay-filter="对应的值"
        var that = data.that, checkbox = data.checkbox, allCheckbox = data.allCheckbox;
        if (checkbox[0].checked) {
            //重置已选的数据
            allCheckbox.prop('checked', false);
            $.each(allCheckbox, function (i, cb) {
                var index = $(cb).parents('tr').eq(0).data('index');
                that.setCheckData(index, false);
            })
            //重新设置当前checkbox为选中
            checkbox.prop("checked", true);
            that.setCheckData(checkbox.parents('tr').eq(0).data('index'), true);
            that.syncCheckAll();
            that.renderForm('checkbox');
        }
        btnTableLoad = false;
        tabTableLoad = false;
        apiTableLoad = false;
        switch (selectBottomTabIndex) {
            case 0:
                renderTable("btn", data.data.resCode);
                break;
            case 1:
                renderTable("tab", data.data.resCode);
                break;
            case 2:
                renderTable("api", data.data.resCode);
                break;
        }
    });
    //监听工具栏
    $('#resource_content .layui-btn').on('click', function () {
        var type = $(this).data('type');
        var resType = $(this).data('res-type');
        active[type] ? active[type].call(this, resType) : '';
    });
    //刷新树的数据
    $('#user_btn_refresh_tree').on('click', function (e) {
        if (appAndResourceTree) {
            appAndResourceTree.reAsyncChildNodes(null, "refresh");
        }
    });
    var nodeList = [];
    //搜索树的数据
    $('#user_search_tree_org').bind('input', function (e) {
        if (appAndResourceTree && $(this).val() != "") {
            nodeList = appAndResourceTree.getNodesByParamFuzzy("name", $(this).val());
            updateNodes(true);
        } else {
            updateNodes(false);
        }
    });

    //刷新树节点
    function updateNodes(highlight) {
        for (var i = 0, l = nodeList.length; i < l; i++) {
            nodeList[i].highlight = highlight;
            appAndResourceTree.updateNode(nodeList[i]);
            if (highlight) {
                appAndResourceTree.expandNode(appAndResourceTree.getNodeByParam("code", nodeList[i]["parentCode"]), true, false, null, null);
            }
        }
    }
})