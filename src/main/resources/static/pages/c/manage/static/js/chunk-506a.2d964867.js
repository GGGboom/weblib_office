(window.webpackJsonp=window.webpackJsonp||[]).push([["chunk-506a"],{"1zWz":function(e,t,o){},"37pw":function(e,t,o){"use strict";var n=o("z66G");o.n(n).a},"44Km":function(e,t,o){"use strict";o.r(t);var n=o("QbLZ"),r=o.n(n),a=o("c9dD"),i={name:"newGroupDialog",data:function(){return{visible:!1,form:{id:null,name:null},isEdit:!1,rules:{name:[{required:!0,message:"请输入机构名称",trigger:"blur"}]}}},props:{title:{type:String,default:"新建"}},created:function(){this.$root.$on("new-group-dialog",this.handleFunc)},methods:{handleClose:function(){this.visible=!1},handleFunc:function(e){var t=this;this.visible=e.visible,this.form.id=-1===e.id?0:e.id,void 0!==e.name?(this.form.name=e.name,this.isEdit=!0,this.$nextTick(function(){t.$refs.inputName.focus()})):(this.isEdit=!1,this.form.name="")},submitFunc:function(e){var t=this;this.isEdit?this.$refs[e].validate(function(e){e&&Object(a.h)({id:t.form.id,name:t.form.name}).then(function(e){e=e.data,t.$message({message:"修改成功",type:"success"}),t.visible=!1,t.$root.$emit("refresh-tree-node",{name:t.form.name,edit:!0,id:t.form.id})})}):this.$refs[e].validate(function(e){if(!e)return!1;Object(a.c)({id:t.form.id,name:t.form.name}).then(function(e){e=e.data,t.$message({message:"创建成功",type:"success"}),t.visible=!1,t.$root.$emit("refresh-tree-node",{name:t.form.name,id:e.members[0].id,isLeaf:!1})})})}}},s=(o("5VT3"),o("KHd+")),l=Object(s.a)(i,function(){var e=this,t=e.$createElement,o=e._self._c||t;return o("div",[o("el-dialog",{attrs:{title:e.title+"组织",visible:e.visible,width:"350px","before-close":e.handleClose},on:{"update:visible":function(t){e.visible=t}}},[o("el-form",{ref:"groupForm",attrs:{rules:e.rules,model:e.form}},[o("el-form-item",{attrs:{label:"",prop:"name"}},[o("el-input",{ref:"inputName",attrs:{autoComplete:"off",placeholder:"请输入机构名称"},model:{value:e.form.name,callback:function(t){e.$set(e.form,"name",t)},expression:"form.name"}})],1)],1),e._v(" "),o("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[o("el-button",{on:{click:function(t){e.visible=!1}}},[e._v("取 消")]),e._v(" "),o("el-button",{attrs:{type:"primary"},on:{click:function(t){e.submitFunc("groupForm")}}},[e._v("确 定")])],1)],1)],1)},[],!1,null,"19983127",null);l.options.__file="newGroupDialog.vue";var c=l.exports,u={name:"newUserGroupDialog",data:function(){return{visible:!1,form:{id:null,name:null},isEdit:!1,rules:{name:[{required:!0,message:"请输入机构名称",trigger:"blur"}]}}},props:{title:{type:String,default:"新建"}},created:function(){this.$root.$on("new-userGroup-dialog",this.handleFunc)},methods:{handleClose:function(){this.visible=!1},handleFunc:function(e){var t=this;console.log(e),this.visible=e.visible,this.form.id=e.id,void 0!==e.name?(this.form.name=e.name,this.isEdit=!0,this.$nextTick(function(){t.$refs.inputName.focus()})):(this.isEdit=!1,this.form.name="")},submitFunc:function(e){var t=this;this.isEdit?this.$refs[e].validate(function(e){e&&Object(a.i)({id:t.form.id,name:t.form.name}).then(function(e){e=e.data,t.$message({message:"修改成功",type:"success"}),t.visible=!1,t.$root.$emit("refresh-tree-node",{name:t.form.name,edit:!0,id:t.form.id})})}):this.$refs[e].validate(function(e){if(!e)return!1;Object(a.e)({id:t.form.id,name:t.form.name}).then(function(e){e=e.data,t.$message({message:"创建成功",type:"success"}),t.visible=!1,t.$root.$emit("refresh-tree-node",{name:t.form.name,id:e.members[0].id,isLeaf:!0})})})}}},d=(o("37pw"),Object(s.a)(u,function(){var e=this,t=e.$createElement,o=e._self._c||t;return o("div",[o("el-dialog",{attrs:{title:e.title+"用户组",visible:e.visible,width:"350px","before-close":e.handleClose},on:{"update:visible":function(t){e.visible=t}}},[o("el-form",{ref:"teamForm",attrs:{rules:e.rules,model:e.form}},[o("el-form-item",{attrs:{label:"",prop:"name"}},[o("el-input",{ref:"inputName",attrs:{autoComplete:"off",placeholder:"请输入用户组名称"},model:{value:e.form.name,callback:function(t){e.$set(e.form,"name",t)},expression:"form.name"}})],1)],1),e._v(" "),o("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[o("el-button",{on:{click:function(t){e.visible=!1}}},[e._v("取 消")]),e._v(" "),o("el-button",{attrs:{type:"primary"},on:{click:function(t){e.submitFunc("teamForm")}}},[e._v("确 定")])],1)],1)],1)},[],!1,null,"40547d72",null));d.options.__file="newUserGroupDialog.vue";var m=d.exports,f=o("U/5H"),p=o.n(f),h={name:"org",components:{newUserGroupDialog:m,newGroupDialog:c},data:function(){return{treeData:[],loading:!1,dialogTitle:"新建",sortable:[],buttonPermission:{createFolder:!1,createTeam:!0},treeNode:{id:-1,type:null,name:null},visibleTree:!0,defaultProps:{label:"text",isLeaf:function(e,t){return"team"===e.type},key:"id"}}},created:function(){this.$root.$on("refresh-tree-node",this.refeshTreeNode),this.$root.$on("user-move-end",this.userMoveEnd)},methods:{refeshTreeNode:function(e){e.edit?this.$refs.tree.getNode(e.id).data.text=e.name:this.$refs.tree.append({fullName:e.name,text:e.name,id:e.id,leaf:e.isLeaf,system:!1,type:e.isLeaf?"team":"folder"},this.treeNode.id)},loadNode:function(e,t){var o=this;return 0===e.level?(setTimeout(function(){o.$refs.tree.setCurrentKey(0),o.getAllPerson(),o.setSort(),o.setLeftContainerSort()},500),t([{text:"全部用户",id:0,fullName:"全部用户",system:!1,type:"team"},{text:"根目录",id:-1,fullName:"根目录",system:!1,leaf:!1}])):e.level>1?(Object(a.k)({folderOnly:!1,id:e.data.id,type:"folder",withoutLeaf:!0}).then(function(e){e=e.data,o.treeData=e.members,t(o.treeData),o.setSort()}),!1):void Object(a.k)({folderOnly:!1,id:0,type:"folder",withoutLeaf:!0}).then(function(e){e=e.data,o.treeData=e.members,t(o.treeData)})},handleDragStart:function(e,t){console.log("drag start",e)},handleDragEnter:function(e,t,o){console.log("tree drag enter: ",t.label)},handleDragLeave:function(e,t,o){console.log("tree drag leave: ",t.label)},handleDragOver:function(e,t,o){console.log("tree drag over: ",t.label)},handleDragEnd:function(e,t,o,n){console.log("tree drag end: ",t&&t.label,o)},handleDrop:function(e,t,o,n){console.log("tree drop: ",t.label,o)},allowDrop:function(e,t,o){return"二级 3-1"!==t.data.label||"inner"!==o},allowDrag:function(e){return-1===e.data.label.indexOf("三级 3-2-2")},nodeClick:function(e){var t=e.id,o=e.type,n=e.text;if(this.treeNode.id=t,this.treeNode.type=o,this.treeNode.name=n,-1===e.id)return this.buttonPermission.createTeam=!0,this.buttonPermission.createFolder=!1,!1;this.buttonPermission.createTeam=!1,this.buttonPermission.createFolder=!1,"team"===e.type&&(this.buttonPermission.createTeam=!0,this.buttonPermission.createFolder=!0,0===e.id?this.getAllPerson():this.getMembers(e))},handleCommand:function(e){switch(e){case"newGroup":this.dialogTitle="新建",this.$root.$emit("new-group-dialog",{visible:!0,id:this.treeNode.id});break;case"newTeam":this.dialogTitle="新建",this.$root.$emit("new-userGroup-dialog",{visible:!0,id:this.treeNode.id})}},handleEdit:function(e){var t=this;switch(e){case"edit":this.dialogTitle="编辑","folder"===this.treeNode.type?this.$root.$emit("new-group-dialog",r()({visible:!0},this.treeNode)):this.$root.$emit("new-userGroup-dialog",r()({visible:!0},this.treeNode));break;case"delete":-1===this.treeNode.id?this.$message({message:"不能删除根节点",type:"warning"}):this.$confirm("此操作将永久删除该节点, 是否继续?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then(function(){t.loading=!0,"team"===t.treeNode.type?Object(a.g)({id:t.treeNode.id}).then(function(e){"200"===(e=e.data).code&&t.$refs.tree.remove(t.treeNode.id),t.loading=!1}).catch(function(e){t.loading=!1}):Object(a.f)({id:t.treeNode.id}).then(function(e){"200"===(e=e.data).code&&t.$refs.tree.remove(t.treeNode.id),t.loading=!1}).catch(function(e){t.loading=!1})})}},getAllPerson:function(){this.$root.$emit("advance-search-callback")},getMembers:function(e){this.$root.$emit("grouper-tree-callback",e)},setLeftContainerSort:function(){var e=document.querySelector(".leftContainerBody");p.a.create(e,{ghostClass:"sortable-ghost",sort:!1,dragoverBubble:!1,group:{name:"leftContainer",put:["userList"]},setData:function(e){e.setData("Text","")},onMove:function(e){console.log(e)},onEnd:function(e){console.log(e)}})},setSort:function(){},userMoveEnd:function(e){this.sortable.length>0&&(this.sortable.forEach(function(e){e.destroy()}),this.setSort())}}},g=(o("Vtp7"),Object(s.a)(h,function(){var e=this,t=e.$createElement,o=e._self._c||t;return o("div",{directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],staticClass:"leftContainer",attrs:{"element-loading-text":"加载中...","element-loading-spinner":"el-icon-loading","element-loading-background":"rgba(0, 0, 0, 0.8)"}},[o("el-header",[o("el-row",[o("el-col",{attrs:{span:16}},[o("div",{staticClass:"grid-content bg-purple cardTitle"},[e._v("\n          用户组织/组\n        ")])]),e._v(" "),o("el-col",{attrs:{span:8}},[o("el-dropdown",{attrs:{trigger:"click"},on:{command:e.handleCommand}},[o("span",{staticClass:"el-dropdown-link"},[o("el-button",{attrs:{type:"info",icon:"el-icon-plus"}})],1),e._v(" "),o("el-dropdown-menu",{attrs:{slot:"dropdown"},slot:"dropdown"},[o("el-dropdown-item",{attrs:{command:"newGroup",disabled:e.buttonPermission.createFolder}},[e._v("新建组织")]),e._v(" "),o("el-dropdown-item",{attrs:{command:"newTeam",disabled:e.buttonPermission.createTeam}},[e._v("新建用户组")])],1)],1),e._v(" "),o("el-dropdown",{attrs:{trigger:"click"},on:{command:e.handleEdit}},[o("span",{staticClass:"el-dropdown-link"},[o("el-button",{attrs:{type:"info",icon:"el-icon-more"}})],1),e._v(" "),o("el-dropdown-menu",{attrs:{slot:"dropdown"},slot:"dropdown"},[o("el-dropdown-item",{attrs:{command:"edit"}},[e._v("编辑")]),e._v(" "),o("el-dropdown-item",{attrs:{command:"delete"}},[e._v("删除")])],1)],1)],1)],1)],1),e._v(" "),o("div",{staticClass:"leftContainerBody"},[e.visibleTree?o("el-tree",{ref:"tree",staticClass:"groupsTree",attrs:{load:e.loadNode,lazy:"","node-key":"id","default-expanded-keys":[-1],draggable:"","allow-drop":e.allowDrop,"allow-drag":e.allowDrag,props:e.defaultProps},on:{"node-drag-start":e.handleDragStart,"node-drag-enter":e.handleDragEnter,"node-drag-leave":e.handleDragLeave,"node-drag-over":e.handleDragOver,"node-drag-end":e.handleDragEnd,"node-drop":e.handleDrop,"node-click":e.nodeClick},scopedSlots:e._u([{key:"default",fn:function(t){var n=t.node,r=t.data;return o("span",{staticClass:"nodeSpan",attrs:{title:n.label,rel:r.id,relName:n.label,type:r.type}},[e._v("\n        "+e._s(n.label)+"\n      ")])}}])}):e._e()],1),e._v(" "),o("new-group-dialog",{attrs:{title:e.dialogTitle}}),e._v(" "),o("new-user-group-dialog",{attrs:{title:e.dialogTitle}})],1)},[],!1,null,"d53df898",null));g.options.__file="org.vue";var b=g.exports,v=o("t3Un");var w={name:"newUserDialog",data:function(){var e=this;return{visible:!1,form:{account:null,password:null,name:null,company:null,department:null,email:null,phone:null,mobile:null,im:null,confirm:null,position:null},isEdit:!1,rules:{account:[{required:!0,message:"请输入用户名",trigger:"blur"}],name:[{required:!0,message:"请输入用户真实姓名",trigger:"blur"}],password:[{validator:function(t,o,n){""===o?n(new Error("请输入密码")):(""!==e.form.confirm&&e.$refs.userDialogForm.validateField("confirm"),n())},required:!0,trigger:"blur"}],confirm:[{validator:function(t,o,n){""===o?n(new Error("请再次输入密码")):o!==e.form.password?n(new Error("两次输入密码不一致!")):n()},required:!0,trigger:"blur"}]}}},computed:{showOrHide:function(){return this.isEdit}},props:{title:{type:String,default:"新建"}},created:function(){this.$root.$on("new-user-dialog",this.handleFunc)},methods:{handleClose:function(){this.visible=!1},handleFunc:function(e){var t=this;this.visible=e.visible,this.form.id=e.id,void 0!==e.account?(this.form.account=e.account,this.form.name=e.name,this.form.company=e.company,this.form.department=e.department,this.form.email=e.email,this.form.phone=e.phone,this.form.mobile=e.mobile,this.form.im=e.im,this.form.position=e.position,this.isEdit=!0,this.$nextTick(function(){t.$refs.account.focus()})):(this.isEdit=!1,this.form.name="")},submitFunc:function(e){var t=this;this.isEdit?this.$refs[e].validate(function(e){e&&function(e){return Object(v.a)({url:"/user/modifyAccount.action",method:"post",params:e})}({account:t.form.account,name:t.form.name,password:t.form.password,company:t.form.company,department:t.form.department,email:t.form.email,phone:t.form.phone,mobile:t.form.mobile,im:t.form.im,position:t.form.position}).then(function(e){e=e.data,t.$message({message:"修改成功",type:"success"}),t.visible=!1,t.$root.$emit("refresh-userlist")})}):this.$refs[e].validate(function(e){if(!e)return!1;(function(e){return Object(v.a)({url:"/user/register.action",method:"post",params:e})})({account:t.form.account,name:t.form.name,password:t.form.password,company:t.form.company,department:t.form.department,email:t.form.email,phone:t.form.phone,mobile:t.form.mobile,im:t.form.im,position:t.form.position}).then(function(e){e=e.data,t.$message({message:"创建成功",type:"success"}),t.visible=!1,t.$root.$emit("refresh-userlist")})})}}},_=(o("cStc"),Object(s.a)(w,function(){var e=this,t=e.$createElement,o=e._self._c||t;return o("div",[o("el-dialog",{attrs:{title:e.title+"用户",visible:e.visible,width:"650px","before-close":e.handleClose},on:{"update:visible":function(t){e.visible=t}}},[o("el-form",{ref:"userDialogForm",attrs:{rules:e.rules,model:e.form}},[o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:12}},[o("el-form-item",{attrs:{label:"用户名",prop:"account"}},[o("el-input",{ref:"account",attrs:{disabled:e.showOrHide,autoComplete:"off",placeholder:"请输入用户名"},model:{value:e.form.account,callback:function(t){e.$set(e.form,"account",t)},expression:"form.account"}})],1)],1),e._v(" "),o("el-col",{attrs:{span:12}},[o("el-form-item",{attrs:{label:"真实姓名",prop:"name"}},[o("el-input",{attrs:{autoComplete:"off",placeholder:"请输入用户真实姓名"},model:{value:e.form.name,callback:function(t){e.$set(e.form,"name",t)},expression:"form.name"}})],1)],1)],1),e._v(" "),e.showOrHide?e._e():o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:12}},[o("el-form-item",{attrs:{label:"用户密码",prop:"password"}},[o("el-input",{attrs:{type:"password",autoComplete:"new-password",placeholder:"请输入用户密码"},model:{value:e.form.password,callback:function(t){e.$set(e.form,"password",t)},expression:"form.password"}})],1)],1),e._v(" "),o("el-col",{attrs:{span:12}},[o("el-form-item",{attrs:{label:"确认密码",prop:"confirm"}},[o("el-input",{attrs:{type:"password",autoComplete:"new-password",placeholder:"请确认密码"},model:{value:e.form.confirm,callback:function(t){e.$set(e.form,"confirm",t)},expression:"form.confirm"}})],1)],1)],1),e._v(" "),o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:12}},[o("el-form-item",{attrs:{label:"公司名称",prop:"company"}},[o("el-input",{attrs:{autoComplete:"off",placeholder:"请输入公司名称"},model:{value:e.form.company,callback:function(t){e.$set(e.form,"company",t)},expression:"form.company"}})],1)],1),e._v(" "),o("el-col",{attrs:{span:12}},[o("el-form-item",{attrs:{label:"所属部门",prop:"department"}},[o("el-input",{attrs:{autoComplete:"off",placeholder:"请输入所属部门"},model:{value:e.form.department,callback:function(t){e.$set(e.form,"department",t)},expression:"form.department"}})],1)],1)],1),e._v(" "),o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:12}},[o("el-form-item",{attrs:{label:"职位",prop:"email"}},[o("el-input",{attrs:{autoComplete:"off",placeholder:"请输入职位"},model:{value:e.form.position,callback:function(t){e.$set(e.form,"position",t)},expression:"form.position"}})],1)],1),e._v(" "),o("el-col",{attrs:{span:12}},[o("el-form-item",{attrs:{label:"邮箱",prop:"email"}},[o("el-input",{attrs:{autoComplete:"off",placeholder:"请输入邮箱"},model:{value:e.form.email,callback:function(t){e.$set(e.form,"email",t)},expression:"form.email"}})],1)],1)],1),e._v(" "),o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:12}},[o("el-form-item",{attrs:{label:"手机号",prop:"mobile"}},[o("el-input",{attrs:{autoComplete:"off",placeholder:"请输入手机号"},model:{value:e.form.mobile,callback:function(t){e.$set(e.form,"mobile",t)},expression:"form.mobile"}})],1)],1),e._v(" "),o("el-col",{attrs:{span:12}},[o("el-form-item",{attrs:{label:"固话号码",prop:"phone"}},[o("el-input",{attrs:{autoComplete:"off",placeholder:"请输入固话号码"},model:{value:e.form.phone,callback:function(t){e.$set(e.form,"phone",t)},expression:"form.phone"}})],1)],1)],1),e._v(" "),o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:12}},[o("el-form-item",{attrs:{label:"社交账号",prop:"im"}},[o("el-input",{attrs:{autoComplete:"off",placeholder:"请输入社交账号"},model:{value:e.form.im,callback:function(t){e.$set(e.form,"im",t)},expression:"form.im"}})],1)],1),e._v(" "),o("el-col",{attrs:{span:12}})],1)],1),e._v(" "),o("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[o("el-button",{on:{click:function(t){e.visible=!1}}},[e._v("取 消")]),e._v(" "),o("el-button",{attrs:{type:"primary"},on:{click:function(t){e.submitFunc("userDialogForm")}}},[e._v("确 定")])],1)],1)],1)},[],!1,null,"f1a1ca80",null));_.options.__file="newUserDialog.vue";var y={name:"user",components:{newUserDialog:_.exports},created:function(){var e=this;this.$root.$on("advance-search-callback",this.callbackFunc),this.$root.$on("grouper-tree-callback",this.groperCallbackFunc),this.$root.$on("refresh-userlist",this.refreshUserList),window.onresize=function(){e.height=document.body.scrollHeight-140}},data:function(){return{tableData:[],members:[],dialogTitle:"新建",height:document.body.scrollHeight-140,total:0,start:0,limit:30,showPage:!0,loading:!1,text:" ",treeId:null,treeName:null,treeType:null,dragId:[],oDiv:null,selection:[],showDrgDiv:!1,dragName:null,dragNum:null,isGetAll:!0,saveData:null}},methods:{getAllPerson:function(){var e=this;this.showPage=!0,this.loading=!0,Object(a.a)({start:this.start,limit:this.limit}).then(function(t){t=t.data,e.loading=!1,e.total=t.totalCount,e.tableData=t.accounts,e.$nextTick(function(){e.setSort()})})},callbackFunc:function(){this.isGetAll=!0,this.start=0,this.getAllPerson()},groperCallbackFunc:function(e){var t=this;this.saveData=e,this.isGetAll=!1,this.showPage=!1,this.loading=!0,this.text=" ",Object(a.k)({folderOnly:!1,id:e.id,type:"team",withoutLeaf:!0}).then(function(e){t.loading=!1,(e=e.data).members.length>0?t.members=e.members.map(function(e){return{account:e.text,name:e.fullName,position:e.positoin,department:e.department,id:e.id,status:e.status}}):t.text="暂无数据",t.tableData=t.members,t.$nextTick(function(){t.setSort()})})},handleSizeChange:function(e){this.tableData=[],this.start=(e-1)*this.limit,this.getAllPerson()},handleCurrentChange:function(e){this.tableData=[],this.start=(e-1)*this.limit,this.getAllPerson()},rowClickHandle:function(e){this.$root.$emit("show-user-detail",e)},handleSelectionChange:function(e){this.selection=e},newUserHandler:function(){this.dialogTitle="新建",this.$root.$emit("new-user-dialog",{visible:!0})},handleEdit:function(e){var t=this,o=this.selection.map(function(e){return e.account});switch(e){case"edit":if(0===this.selection.length)return this.$message({message:"请先选择一个用户",type:"warning",duration:2e3}),!1;if(this.selection.length>1)return this.$message({message:"只能选择一个用户编辑",type:"warning",duration:2e3}),!1;this.dialogTitle="编辑";var n=this.selection[0];this.$root.$emit("new-user-dialog",r()({},n,{visible:!0}));break;case"delete":this.$confirm("是否确认删除？","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then(function(){(function(e){return Object(v.a)({url:"/user/deleteAccount.action",method:"post",params:e})})({account:o.join(",")}).then(function(e){"200"===e.data.code&&(t.isGetAll?t.callbackFunc():t.groperCallbackFunc(t.saveData))})});break;case"expiredAccount":this.$confirm("是否注销用户？","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then(function(){(function(e){return Object(v.a)({url:"/user/expiredAccount.action",method:"post",params:e})})({account:o.join(",")}).then(function(e){"200"===e.data.code&&(t.isGetAll?t.callbackFunc():t.groperCallbackFunc(t.saveData))})});break;case"restoreAccount":this.$confirm("是否激活用户？","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then(function(){(function(e){return Object(v.a)({url:"/user/restoreAccount.action",method:"post",params:e})})({account:o.join(",")}).then(function(e){"200"===e.data.code&&(t.isGetAll?t.callbackFunc():t.groperCallbackFunc(t.saveData))})})}},refreshUserList:function(){this.isGetAll?this.callbackFunc():this.groperCallbackFunc(this.saveData)},setSort:function(){var e=this;this.oDiv=document.querySelector(".dragDiv");var t=this.$refs.dragTable.$el.querySelectorAll(".el-table__body-wrapper > table > tbody")[0];p.a.create(t,{ghostClass:"user-ghost",chosenClass:"user-chosen",dragClass:"user-drag",dragoverBubble:!1,bubbleScroll:!1,sort:!0,group:{name:"userList",pull:"clone",revertClone:!1,put:!1},setData:function(e,t){return!1},onStart:function(t){e.dragId=[],0===e.selection.length?(e.dragId=[parseInt(t.item.querySelector(".account").getAttribute("rel"))],e.dragNum=1,e.dragName=t.item.querySelector(".account").getAttribute("relName")):(e.dragId=e.selection,e.dragNum=e.selection.length,e.dragName=e.selection[0].name+(e.selection.length>1?"等...":"")),e.oDiv.style.left=t.sortable._lastX-30+"px",e.oDiv.style.top=t.sortable._lastY-30+"px",e.treeId=null,e.treeName=null,e.showDrgDiv=!0},onMove:function(t,o){e.oDiv.style.left=o.clientX-30+"px",e.oDiv.style.top=o.clientY-30+"px";var n=o.target,r=n.getAttribute("class").indexOf("nodeSpan")>-1?n:null;if(console.log("--------"),console.log(r),null!==r){var a=r.parentElement.querySelector(".el-tree-node__expand-icon");document.querySelectorAll(".nodeSpan").forEach(function(e){e.setAttribute("class","nodeSpan")});try{r.setAttribute("class","nodeSpan active"),"folder"===r.getAttribute("type")?a.getAttribute("class").indexOf("expanded")<0&&a.click():(e.treeId=r.getAttribute("rel"),e.treeName=r.getAttribute("relName"),e.treeType=r.getAttribute("type"))}catch(e){}}return!1},onEnd:function(t){if(e.showDrgDiv=!1,"team"!==e.treeType)return!1;e.selection.length>0&&(e.dragId=e.dragId.map(function(e){return e.members[0].id})),e.treeId&&null!==e.treeName&&e.$confirm("是否移动到"+e.treeName+"组?","提示",{confirmButtonText:"确定",cancelButtonText:"取消",type:"warning"}).then(function(){(function(e){return Object(v.a)({url:"/grouper/addMemberToTeam.action",method:"post",params:e})})({id:e.treeId,memberId:e.dragId.join(",")}).then(function(t){e.$message({message:"操作成功",type:"success",duration:2e3})})})}})}}},$=(o("Gki+"),Object(s.a)(y,function(){var e=this,t=e.$createElement,o=e._self._c||t;return o("div",{staticClass:"userWrap",staticStyle:{width:"100%"}},[o("el-header",[o("el-col",{attrs:{span:21}},[o("div",{staticClass:"grid-content bg-purple cardTitle"},[e._v("\n        用户\n      ")])]),e._v(" "),o("el-col",{attrs:{span:3}},[o("el-button",{attrs:{type:"info",icon:"el-icon-plus",title:"新建用户"},on:{click:e.newUserHandler}}),e._v(" "),o("el-dropdown",{attrs:{trigger:"click"},on:{command:e.handleEdit}},[o("span",{staticClass:"el-dropdown-link"},[o("el-button",{attrs:{type:"info",icon:"el-icon-more"}})],1),e._v(" "),o("el-dropdown-menu",{attrs:{slot:"dropdown"},slot:"dropdown"},[o("el-dropdown-item",{attrs:{command:"edit"}},[e._v("编辑")]),e._v(" "),o("el-dropdown-item",{attrs:{command:"expiredAccount"}},[e._v("注销")]),e._v(" "),o("el-dropdown-item",{attrs:{command:"restoreAccount"}},[e._v("激活")]),e._v(" "),o("el-dropdown-item",{attrs:{command:"delete"}},[e._v("删除")])],1)],1)],1)],1),e._v(" "),o("el-table",{directives:[{name:"loading",rawName:"v-loading",value:e.loading,expression:"loading"}],ref:"dragTable",staticStyle:{width:"100%"},attrs:{data:e.tableData,height:e.height,"empty-text":e.text,"element-loading-background":"rgba(0, 0, 0, 0.3)","element-loading-text":"加载中..."},on:{"selection-change":e.handleSelectionChange,"row-click":e.rowClickHandle}},[o("el-table-column",{attrs:{type:"selection",width:"30"}}),e._v(" "),o("el-table-column",{attrs:{label:"账号"},scopedSlots:e._u([{key:"default",fn:function(t){return[o("span",{staticClass:"account",staticStyle:{"margin-left":"10px"},attrs:{rel:Object.keys(t.row).indexOf("members")>-1?t.row.members[0].id:t.row.id,relName:t.row.name}},[e._v(e._s(t.row.account))]),e._v(" "),"delete"===t.row.status?o("el-tag",{staticStyle:{"margin-left":"5px"},attrs:{type:"warning"}},[e._v("已注销")]):e._e()]}}])}),e._v(" "),o("el-table-column",{attrs:{prop:"name",label:"真实姓名",width:"110"}}),e._v(" "),o("el-table-column",{attrs:{width:"150",prop:"department",label:"部门"}}),e._v(" "),o("el-table-column",{attrs:{prop:"position",width:"60",label:"岗位"}})],1),e._v(" "),e.showPage?o("el-pagination",{attrs:{"page-size":e.limit,layout:"prev, pager, next, jumper",total:e.total},on:{"size-change":e.handleSizeChange,"current-change":e.handleCurrentChange}}):e._e(),e._v(" "),o("div",{directives:[{name:"show",rawName:"v-show",value:e.showDrgDiv,expression:"showDrgDiv"}],staticClass:"dragDiv"},[o("span",{staticClass:"dragDivName"},[e._v(e._s(e.dragName))]),e._v(" "),o("div",{staticClass:"dragDivIn"},[o("span",{staticClass:"num"},[e._v(e._s(e.dragNum))])])]),e._v(" "),o("new-user-dialog",{attrs:{title:e.dialogTitle}})],1)},[],!1,null,"2f4be798",null));$.options.__file="user.vue";var x=$.exports,k={name:"userDetail",created:function(){this.$root.$on("show-user-detail",this.showUserDetail)},data:function(){return{account:null,company:null,department:null,email:null,im:null,members:null,mobile:null,name:null,phone:null,position:null,status:null}},methods:{showUserDetail:function(e){var t=e.account,o=e.company,n=e.department,r=e.email,a=e.im,i=e.members,s=e.mobile,l=e.name,c=e.phone,u=e.position,d=e.status;this.account=t,this.company=o,this.department=n,this.email=r,this.im=a,this.members=i,this.mobile=s,this.name=l,this.phone=c,this.position=u,this.status=d}}},C=(o("MJ4z"),Object(s.a)(k,function(){var e=this,t=e.$createElement,o=e._self._c||t;return o("div",[o("el-header",[e._v("详细资料")]),e._v(" "),o("div",{staticClass:"detailWrap"},[o("el-form",{attrs:{"label-position":"left",inline:""}},[o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:24}},[o("el-form-item",{attrs:{label:"账号"}},[o("span",[e._v(e._s(this.account))])])],1)],1),e._v(" "),o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:24}},[o("el-form-item",{attrs:{label:"真实姓名"}},[o("span",[e._v(e._s(this.name))])])],1)],1),e._v(" "),o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:24}},[o("el-form-item",{attrs:{label:"部门"}},[o("span",[e._v(e._s(this.department))])])],1)],1),e._v(" "),o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:24}},[o("el-form-item",{attrs:{label:"职位"}},[o("span",[e._v(e._s(this.position))])])],1)],1),e._v(" "),o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:24}},[o("el-form-item",{attrs:{label:"邮箱"}},[o("span",[e._v(e._s(this.email))])])],1)],1),e._v(" "),o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:24}},[o("el-form-item",{attrs:{label:"手机号码"}},[o("span",[e._v(e._s(this.mobile))])])],1)],1),e._v(" "),o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:24}},[o("el-form-item",{attrs:{label:"固定电话"}},[o("span",[e._v(e._s(this.phone))])])],1)],1),e._v(" "),o("el-row",{attrs:{gutter:20}},[o("el-col",{attrs:{span:24}},[o("el-form-item",{attrs:{label:"社交账号"}},[o("span",[e._v(e._s(this.im))])])],1)],1)],1)],1)],1)},[],!1,null,"263946a0",null));C.options.__file="userDetail.vue";var D={name:"userIndex",components:{org:b,user:x,userDetail:C.exports},data:function(){return{}},created:function(){},methods:{}},T=(o("GB7p"),Object(s.a)(D,function(){var e=this.$createElement,t=this._self._c||e;return t("div",[t("el-container",{staticClass:"mainContainer",staticStyle:{height:"100%"}},[t("el-aside",{staticClass:"user-aside",staticStyle:{"background-color":"#1e2022",overflow:"hidden"},attrs:{width:"260px"}},[t("org")],1),this._v(" "),t("el-container",{staticClass:"middleWrap"},[t("user")],1),this._v(" "),t("el-aside",[t("user-detail")],1)],1)],1)},[],!1,null,"590796c8",null));T.options.__file="index.vue";t.default=T.exports},"5VT3":function(e,t,o){"use strict";var n=o("x73Z");o.n(n).a},"78ax":function(e,t,o){},GB7p:function(e,t,o){"use strict";var n=o("1zWz");o.n(n).a},"Gki+":function(e,t,o){"use strict";var n=o("78ax");o.n(n).a},Jqbs:function(e,t,o){},MJ4z:function(e,t,o){"use strict";var n=o("vMV6");o.n(n).a},Vtp7:function(e,t,o){"use strict";var n=o("Jqbs");o.n(n).a},c9dD:function(e,t,o){"use strict";o.d(t,"k",function(){return r}),o.d(t,"c",function(){return a}),o.d(t,"e",function(){return i}),o.d(t,"g",function(){return s}),o.d(t,"f",function(){return l}),o.d(t,"h",function(){return c}),o.d(t,"i",function(){return u}),o.d(t,"a",function(){return d}),o.d(t,"j",function(){return m}),o.d(t,"b",function(){return f}),o.d(t,"d",function(){return p});var n=o("t3Un");function r(e){return Object(n.a)({url:"/grouper/grouperTree.action",method:"post",params:e})}function a(e){return Object(n.a)({url:"/grouper/createFolder.action",method:"post",params:e})}function i(e){return Object(n.a)({url:"/grouper/createTeam.action",method:"post",params:e})}function s(e){return Object(n.a)({url:"/grouper/deleteTeam.action",method:"post",params:e})}function l(e){return Object(n.a)({url:"/grouper/deleteFolder.action",method:"post",params:e})}function c(e){return Object(n.a)({url:"/grouper/editFolder.action",method:"post",params:e})}function u(e){return Object(n.a)({url:"/grouper/editTeam.action",method:"post",params:e})}function d(e){return Object(n.a)({url:"/user/advancedSearch.action",method:"post",params:e})}function m(e){return Object(n.a)({url:"/category/getCategoriesWithManagedGroup.action",method:"post",params:e})}function f(e){return Object(n.a)({url:"/category/createCategory.action",method:"post",params:e})}function p(e){return Object(n.a)({url:"/group/createGroup.action",method:"post",params:e})}},cStc:function(e,t,o){"use strict";var n=o("i04f");o.n(n).a},i04f:function(e,t,o){},vMV6:function(e,t,o){},x73Z:function(e,t,o){},z66G:function(e,t,o){}}]);