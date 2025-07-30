package top.zdever.kline.config

data class LineConfig(
    var name:String,
    var id:Int,
    var cycle: Int,
    var width: Float,
    var color: Int,
    var isOpen: Boolean
)