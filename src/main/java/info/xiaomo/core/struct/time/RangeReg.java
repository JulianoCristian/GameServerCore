package info.xiaomo.core.struct.time;

import info.xiaomo.core.common.utils.SymbolUtil;

/**
 * 区间表达
 *
 *
 * <p>
 * 2017年8月2日 上午9:53:24
 */
public class RangeReg {

	private Type type;
	private String range;

	public RangeReg(String range) {
		if (range.indexOf("-") > 0) {//区间划分
			type = Type.TO;
		} else if (range.indexOf(",") > 0) {//或划分
			type = Type.OR;
		} else if (SymbolUtil.isNullOrEmpty(range)) {
			type = Type.NULL;
		} else {
			type = Type.ONLY;
		}
		this.range = range.replace("[", "").replace("]", "");
//        List<String> parseArray = JSON.parseArray(range, String.class);
//        ranges = parseArray.toArray(new String[parseArray.size()]);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getRange() {
		return range;
	}

	public void setRange(String range) {
		this.range = range;
	}

	public enum Type {
		NULL,//空
		ONLY,//唯一
		OR,// 通过,和/分割表示
		TO,// 通过-分割表示
	}


}
