package blade.jdbc.dialect;

import blade.jdbc.annotation.Column;

public class PostgresDialect extends DefaultDialect {

	@Override
	public String getCreateTableSql(Class<?> clazz) {

		StringBuilder buf = new StringBuilder();

		ModelMeta modelMeta = getModelInfo(clazz);
		buf.append("create table ");
		buf.append(modelMeta.table);
		buf.append(" (");

		boolean needsComma = false;
		for (Property prop : modelMeta.propertyMap.values()) {

			if (needsComma) {
				buf.append(',');
			}
			needsComma = true;

			Column columnAnnot = prop.columnAnnotation;
			if (columnAnnot == null) {

				buf.append(prop.name);
				buf.append(" ");
				if (prop.isGenerated) {
					buf.append(" serial");
				} else {
					buf.append(getColType(prop.dataType, 255, 10, 2));
				}

			} else {
				if (columnAnnot.columnDefinition() == null) {

					// let the column def override everything
					buf.append(columnAnnot.columnDefinition());

				} else {

					buf.append(prop.name);
					buf.append(" ");
					if (prop.isGenerated) {
						buf.append(" serial");
					} else {
						buf.append(getColType(prop.dataType, columnAnnot.length(), columnAnnot.precision(),
								columnAnnot.scale()));
					}

					if (columnAnnot.unique()) {
						buf.append(" unique");
					}

					if (!columnAnnot.nullable()) {
						buf.append(" not null");
					}
				}
			}
		}

		if (modelMeta.primaryKeyName != null) {
			buf.append(", primary key (");
			buf.append(modelMeta.primaryKeyName);
			buf.append(")");
		}

		buf.append(")");

		return buf.toString();
	}

}
