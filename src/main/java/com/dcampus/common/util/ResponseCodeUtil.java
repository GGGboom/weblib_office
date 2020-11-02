package com.dcampus.common.util;

/**
 * 错误码与错误信息的封装
 */
public enum ResponseCodeUtil {
    //////////////////////参数相关，错误代码相同，但是错误信息不同/////////////////////////////////

    //传了错误格式的参数
    ERROR_PARAM("601", "请传入正确的参数"),

    //您选择的上层组织不存在
    ERROR_PARAM_SCHEDULE_NOT_EXIST("601", "对不起，您选择的上层组织不存在"),

    //对不起，操作失败，已经有了该条数据
    ERROR_PARAM_SCHEDULE_EXIST("601", "对不起，操作失败，已经有了该条数据。"),

    //请传入团队的编号，无此团队
    ERROR_PARAM_TEAM("601", "请传入团队的编号，无此团队"),

    //该学生不在该团队中
    ERROR_PARAM_TEAM_STUDENT("601", "该学生不在该团队中"),

    //该学生不在该课程中
    ERROR_PARAM_COURSE_STUDENT("601", "该学生不在该课程中"),

    //该章节不属于该课程
    ERROR_PARAM_SECTION_COURSE("601", "该章节不属于该课程"),

    //该团队不属于该课程
    ERROR_PARAM_TEAM_COURSE("601", "该团队不属于该课程"),

    //该团队不属于该实验
    ERROR_PARAM_TEAM_HOMEWORK("601", "该团队不属于该实验"),

    //该作业不属于该章节
    ERROR_PARAM_HOMEWORK_SECTION("601", "该作业不属于该章节"),

    //该测验不属于该章节
    ERROR_PARAM_QUIZ_SECTION("601", "该测验不属于该章节"),

    //该知识点不属于该课程
    ERROR_PARAM_UNIT_COURSE("601", "该知识点不属于该课程"),

    //该讨论不属于该课程
    ERROR_PARAM_DISCUSS_COURSE("601", "该讨论不属于该课程"),
    //该知识点不属于该章节
    ERROR_PARAM_UNIT_SECTION("601", "该知识点不属于该章节"),

    //该团队没有该学生
    ERROR_PARAM_STUDENT_TEAM("601","该团队没有该学生"),

    //该回复不属于该讨论
    ERROR_PARAM_REPLY_DISCUSSION("601", "该回复不属于该讨论"),

    //该通知不属于该课程
    ERROR_PARAM_MESSAGE_COURSE("601", "该通知不属于该课程"),

    //请传入正确的测验编号，无此测验
    ERROR_PARAM_QUIZ("601", "请传入正确的测验编号，无此测验"),

    //请传入正确的通知编号，无此通知
    ERROR_PARAM_MESSAGE("601", "请传入正确的通知编号，无此通知"),

    //请传入正确的作业类型
    ERROR_PARAM_HOMEWORK_TYPE("601", "请传入正确的作业类型"),

    //请传入正确的回复编号，无此回复
    ERROR_PARAM_REPLY("601", "请传入正确的回复编号，无此回复"),

    //请传入正确的讨论编号，无此讨论
    ERROR_PARAM_DISCUSS("601", "请传入正确的讨论编号，无此讨论"),

    //作业类型为测验的话，需要调用其他接口
    ERROR_PARAM_QUIZ_TYPE("601", "作业类型为测验的话，需要调用其他接口"),

    //作业类型只能为作业与实验
    ERROR_PARAM_HOMEWORK_TYPE_TWO("601", "作业类型只能为作业与实验"),

    //该学生无此作业
    ERROR_PARAM_HOMEWORK_STUDENT("601", "该学生无此作业"),

    //查询条件至少要三个及以上字符
    ERROR_PARAM_QUERY("601", "查询条件至少要三个及以上字符"),

    //开始时间要小于结束时间
    ERROR_PARAM_TIME("601", "开始时间要小于结束时间"),

    //该用户已经是该课程的学生
    ERROR_PARAM_STUDENT_EXIST("601", "该用户已经是该课程的学生"),

    //该用户不是该课程的学生
    ERROR_PARAM_STUDENT_NO_EXIST("601", "该用户不是该课程的学生"),

    //该用户已经是该课程的教师
    ERROR_PARAM_TEACHER_EXIST("601", "该用户已经是该课程的教师"),

    //不能增加开课老师
    ERROR_PARAM_TEACHER_TYPE("601", "不能对开课老师操作"),

    //请传入教师的编号
    ERROR_PARAM_TEACHER_ID("601", "请传入教师的编号"),

    //该用户不是该课程的教师
    ERROR_PARAM_TEACHER_NO_EXIST("601", "该用户不是该课程的教师"),

    //该用户已经是该团队的教师
    ERROR_PARAM_TEACHER_TEAM_EXIST("601", "该用户已经是该团队的教师"),

    //请传入正确的课程编号，无此课程
    ERROR_PARAM_COURSE("601", "请传入正确的课程编号，无此课程"),

    //请传入正确的组织编号，无此组织
    ERROR_PARAM_SCHEDULE("601", "请传入正确的组织编号，无此组织"),

    //请传入正确的章节编号，无此章节
    ERROR_PARAM_SECTION("601", "请传入正确的章节编号，无此章节"),

    //请传入正确的知识点编号，无此知识点
    ERROR_PARAM_UNIT("601", "请传入正确的知识点编号，无此知识点"),

    //请传入正确的作业编号，无此作业
    ERROR_PARAM_HOMEWORK("601", "请传入正确的作业编号，无此作业"),

    //请传入正确的实验编号，无此实验
    ERROR_PARAM_EXPERIMENT("601", "请传入正确的实验编号，无此实验"),

    //无此用户
    ERROR_PARAM_USER("601", "请传入正确的用户编号，无此用户"),

    //传入的不是登录者的
    ERROR_PARAM_USER_TWO("601", "必须要该登录学生的调用，传入正确的用户编号"),

    //已经有该用户了，请调用其他接口新增
    ERROR_PARAM_USER_EXIST("601", "已经有该用户了，请调用其他接口新增"),

    //用户密码过于简单, 请使用大、小写字母、数字、 特殊字符中的任意三种组合
    ERROR_PARAM_PASSWORD("601", "用户密码过于简单, 请使用大、小写字母、数字、 特殊字符中的任意三种组合"),


    //请检查正确格式的邮箱
    ERROR_PARAM_EMAIL("601", "请检查正确格式的邮箱"),

    //该学生无成绩单
    ERROR_PARAM_GRADEBOOK("601", "该课程该学生无成绩单"),

    //分数需要为数字
    ERROR_PARAM_SCORE("601", "分数需要为数字"),

    //你已经答过题了
    ERROR_PARAM_QUIZ_FINISH("601", "你已经答过题了"),

    //请传入用户的用户名
    ERROR_PARAM_ACCOUNT("601", "请传入用户的用户名"),

    //请传入正确的签到设置编号，无此签到设置
    ERROR_PARAM_SIGNIN("601", "请传入正确的签到设置编号，无此签到设置"),

    //签到距离不再50米以内
    ERROR_PARAM_SIGNIN_DISTANCE("601", "签到距离不在50米以内"),

    //若传入type，请传入5个type
    ERROR_PARAM_TYPE("601", "若传入type，请传入5个type"),

    //请传入正确的字典类别，无此字典数据
    ERROR_PARAM_DICT_TYPE("601", "请传入正确的字典类别，无此字典数据"),

    //请传入正确的字典编号，无此字典数据
    ERROR_PARAM_DICT("601", "请传入正确的字典编号，无此字典数据"),

    //已经存在该字典数据，请重新传入
    ERROR_PARAM_DICT_EXIST("601", "已经存在该字典数据"),


    //若传入ratio，请确定都为整数
    ERROR_PARAM_RATIO("601", "若传入ratio，请确定都为整数"),

    //若传入ratio，请确定和为100
    ERROR_PARAM_RATIO_TOTAL("601", "若传入ratio，请确定和为100"),


    //学习的进度需要在0-1范围内
    ERROR_PARAM_PROGRESS("601", "学习的进度需要在0-1范围内"),

    //请传入正确的角色编号，无此角色
    ERROR_PARAM_ROLE("601", "请传入正确的角色编号，无此角色"),

    //请传入正确的操作日志编号，无此操作日志
    ERROR_PARAM_OPERATION_LOG("601", "请传入正确的操作日志编号，无此操作日志"),

    //请传入正确的异常日志编号，无此异常日志
    ERROR_PARAM_EXCEPTION_LOG("601", "请传入正确的异常日志编号，无此异常日志"),

    //必传参数传了空参数
    EMPTY_PARAM("602", "必传参数不能为空"),

    //组织编号不能为空
    EMPTY_PARAM_SCHEDULE_CODE("602", "请传入组织的编号"),

    //组织名称不能为空
    EMPTY_PARAM_SCHEDULE_NAME("602", "请传入组织的名称"),

    //请传入角色的编号
    EMPTY_PARAM_ROLE("602", "请传入角色的编号"),

    //请传入角色的名称
    EMPTY_PARAM_ROLE_NAME("602", "请传入角色的名称"),

    //请传入字典类别
    EMPTY_PARAM_DICT_TYPE("602", "请传入字典类别"),

    //请传入字典名称
    EMPTY_PARAM_DICT_NAME("602", "请传入字典名称"),

    //请传入字典代码值
    EMPTY_PARAM_DICT_CODE("602", "请传入字典代码值"),

    //你不是该课程的教师
    ERROR_PARAM_TEACHER("601", "你不是该课程的教师"),

    //你不是该团队的教师
    ERROR_PARAM_TEACHER_TEAM("601", "你不是该团队的教师"),

    //对不起，请传入正确的院系编号
    ERROR_PARAM_SCHEDULE_CODE("601", "对不起，请传入正确的院系编号"),

    ERROR_PARAM_DEPARTMENT_CODE("601", "该用户不是该团队的学生"),

    //请输入团队的编号
    EMPTY_PARAM_TEAM_ID("602", "请输入团队的编号"),

    //请传入团队学生的编号
    EMPTY_PARAM_TEAM_STUDENT_Id("602", "请传入团队学生的编号"),

    //测验的编号
    EMPTY_PARAM_QUIZ_ID("602", "请输入测验的编号"),

    //请传入通知的编号
    EMPTY_PARAM_MESSAGE("602", "请传入通知的编号"),

    //请传入讨论的编号
    EMPTY_PARAM_REPLY_ID("602", "请传入讨论的编号"),

    //请传入讨论的编号
    EMPTY_PARAM_DISCUSS_ID("602", "请传入讨论的编号"),

    //请输入正确的查询类型
    EMPTY_PARAM_TYPE("602", "请输入正确的查询类型"),

    //用户编号不能为空
    EMPTY_PARAM_USER_ID("602", "用户编号不能为空"),

    //账号名与密码为空
    EMPTY_PARAM_USERNAMEPW("602", "账号名或者密码不能为空"),

    //课程编号不能为空
    EMPTY_PARAM_COURSE_ID("602", "课程编号不能为空"),

    //开始时间不能为空
    EMPTY_PARAM_TIME_START("602", "开始时间不能为空"),

    //终止时间不能为空
    EMPTY_PARAM_TIME_END("602", "终止时间不能为空"),

    //课程名称不能为空
    EMPTY_PARAM_COURSE_NAME("602", "课程名称不能为空"),

    //对不起，请传入院系的编号
    EMPTY_PARAM_DEPARTMENT_CODE("602", "对不起，请传入院系的编号"),

    //课程签到的编号不能为空
    EMPTY_PARAM_SIGNIN_ID("602", "课程签到的编号不能为空"),

    //签到的经纬度不能为空
    EMPTY_PARAM_SIGNIN_LONGITUDE("602", "签到的经纬度不能为空"),


    //文件的路径不能为空
    EMPTY_PARAM_FILE_PATH("602", "文件的路径不能为空"),

    //章节编号不能为空
    EMPTY_PARAM_SECTION_ID("602", "章节编号不能为空"),


    //作业编号不能为空
    EMPTY_PARAM_HOMEWORK_ID("602", "作业编号不能为空"),

    //作业名称不能为空
    EMPTY_PARAM_HOMEWORK_NAME("602", "作业名称不能为空"),

    //章节名称不能为空
    EMPTY_PARAM_SECTION_NAME("602", "章节名称不能为空"),

    //作业正文名称不能为空
    EMPTY_PARAM_ATTACH_NAME("602", "作业正文名称不能为空"),

    //作业附件名称不能为空
    EMPTY_PARAM_ATTACH2_NAME("602", "作业附件名称不能为空"),


    //知识点编号不能为空
    EMPTY_PARAM_UNIT_ID("602", "知识点编号不能为空"),

    //知识点名称不能为空
    EMPTY_PARAM_UNIT_NAME("602", "知识点名称不能为空"),

    //////////////////////参数相关，错误代码相同，但是错误信息不同/////////////////////////////////

    //////////////////////weblib相关，错误代码相同，但是错误信息不同/////////////////////////////////

    //与WebLib操作有误
    WEBLIB_OPERATE_ERROR("900", "检查WebLib连接"),

    //weblib登录失败
    WEBLIB_LOGIN_ERROR("900", "webLib登录失败"),

    //weblib创建用户柜子失败
    WEBLIB_CREATE_COURSE_ERROR("900", "webLib创建用户柜子失败"),

    //weblib删除用户柜子失败
    WEBLIB_DELETE_COURSE_ERROR("900", "webLib删除用户柜子失败"),

    //保存用户至用户组失败
    WEBLIB_SAVE_USER_ERROR("900", "保存用户至用户组失败"),

    //修改密码失败
    WEBLIB_SAVE_PASSWORD_ERROR("900", "修改密码失败"),

    //webLib注册用户失败
    WEBLIB_REGISTER_ERROR("900", "webLib注册用户失败"),

    //////////////////////weblib相关，错误代码相同，但是错误信息不同/////////////////////////////////

    //////////////////////用户与登录相关，错误代码相同，但是错误信息不同/////////////////////////////////
    //登录过期
    LOGIN_TIMEOUT("401","登录过期"),

    //登录失败
    LOGIN_FAILED("702", "登录失败"),

    //未登录
    NO_LOGIN("703", "对不起，您还未登录"),

    //没有该用户
    NO_USERNAME_PASSWORD("701", "没有该用户"),


    //////////////////////用户与登录相关，错误代码相同，但是错误信息不同/////////////////////////////////

    //////////////////////权限相关，错误代码相同，但是错误信息不同/////////////////////////////////

    //权限不够，对不起，您不是该课程的教师/创建者
    NOT_PERMIT("555", "对不起，您不是该课程的教师/创建者"),

    //权限不够，对不起，您不是该课程的相关用户
    NOT_PERMIT_USER("555", "对不起，您不是该课程的相关用户"),

    //对不起，您不是该课程的学生
    NOT_PERMIT_STUDENT("555", "对不起，您不是该课程的学生"),

    //权限不够，对不起，您没有创建课程权限
    NOT_PERMIT_CREATOR("555", "对不起，您没有创建课程权限"),


    //权限不够，对不起，您没有教务员权限
    NOT_PERMIT_DEAN("555", "对不起，您没有教务员权限"),


    //权限不够，对不起，您没有管理员权限
    NOT_PERMIT_ADMIN("555", "对不起，您没有管理员权限"),

    //////////////////////权限相关，错误代码相同，但是错误信息不同/////////////////////////////////

    //////////////////////文件操作相关///////////////////////////////////////////////////////////

    //文件不符合规定规则
    ERROR_FILE_GUIDE("803", "文件不符合规定规则"),

    //文件保证为一个sheet
    ERROR_FILE_GUIDE_SHEET("803", "请确保文件中只有一个sheet"),

    //请确保文件的标题不要修改
    ERROR_FILE_GUIDE_TITLE("803", "请确保文件的标题不要修改"),

    //文件创建/写入/文件流失败
    ERROR_FILE_OPERATE("804", "文件操作失败"),

    //没有找到指定文件
    ERROR_FILE_NO_EXIST("804", "没有找到指定文件"),

    //请传入jpg,jpeg,gif,png,xls,xlsx类型的文件
    ERROR_FILE_TYPE("803", "请传入jpg,jpeg,gif,png,xls,xlsx类型的文件"),


    //////////////////////文件操作相关/////////////////////////////////////////////////////////////

    //未知错误
    NOT_KNOW_ERROR("666", "未知错误"),

    //错误操作，例如，已经答过题
    ERROR_OPERATION("800", "错误操作"),

    //方法调用错误
    ERROR_METHOD("801", "该方法无此功能"),
    //提交作业时间过了
    ERROR_SUBMIT_DATE("802", "作业提交时间已截止"),

    //签到时间不对
    ERROR_SINGN_DATE("802", "不在签到时间内"),

    //在进行这一操作之前没有进行某项操作
    ERROR_BEFORE_OPERATE("805", "需要有学生团队，才可进行团队实验的创建");


    private String code;

    private String message;

    ResponseCodeUtil(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
