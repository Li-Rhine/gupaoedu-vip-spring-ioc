
MVC 9大组件

MultipartResolver 多文件上传的组件
LocaleResolver 本地语言环境
ThemeResolver 主题模板处理器
*HandlerMapping 保存Url映射关系
*HandlerAdapter 动态参数适配器
HandlerExceptionResolver 异常拦截器
RequestToViewNameTranslator 视图提取器，从request中获取viewName
*ViewResolvers 视图转换器，模板引擎
FlashMapManager 参数缓存器



HandlerMapping -> HandlerAdapter -> ModelAndView -> ViewResolver -> View

1、初始化Spring MVC的九大组件，今天我们只初始化3个

2、doDispatch
    获得HandlerMapping
    根据HandlerMapping获得HandlerAdpater
    然后再根据HandlerAdapter获得ModleAndView
   根据ModleAndView里面的viewName去获得一个ViewResolver
   根据一个ViewResolver获得一个view
   
   view.render方法渲染，解析模板