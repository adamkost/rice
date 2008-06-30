ALTER TABLE EN_DOC_TYP_T ADD RPT_WRKGRP_ID NUMBER(14) NULL;

CREATE INDEX EN_DOC_TYP_TI7 ON EN_DOC_TYP_T (RPT_WRKGRP_ID);

CREATE INDEX EN_DOC_DMP_TI1
      ON EN_EDL_DMP_T (DOC_TYP_NM, DOC_HDR_ID);

CREATE INDEX EN_EDL_FIELD_DMP_TI1
      ON EN_EDL_FIELD_DMP_T (DOC_HDR_ID, FLD_NM, FLD_VAL);
CREATE INDEX EN_EDL_FIELD_DMP_TI2
      ON EN_EDL_FIELD_DMP_T (FLD_NM, FLD_VAL);