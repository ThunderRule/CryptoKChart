package top.zdever.kline.base

import top.zdever.kline.model.IKLine

/**
 * @description
 *
 * @author bitman
 * @createDate 2024/6/16
 */
interface IDataTool {

    fun calculate(list:List<IKLine>)

}