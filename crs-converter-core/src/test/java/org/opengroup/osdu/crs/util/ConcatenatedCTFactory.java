package org.opengroup.osdu.crs.util;

import org.opengroup.osdu.crs.model.*;
import org.opengroup.osdu.crs.model.v2.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.opengroup.osdu.crs.model.ReferenceConverter.parseSpatialReference;

public class ConcatenatedCTFactory {

    private static List<CompoundTrf> makeCompoundCT(int[] meridians) {
        List<SingleTrf> forwards = new ArrayList<>();
        List<SingleTrf> inverses = new ArrayList<>();
        List<CompoundTrf> compoundCTs = new ArrayList<>();
        for (int i = 0; i < meridians.length - 1; i++) {
            int m1 = meridians[i];
            int m2 = meridians[i + 1];
        }
        int sourceMeridian = meridians[0];
        int targetMeridian = meridians[meridians.length - 1];
        String nameTrf = String.format("G-%d to G-%d", sourceMeridian, targetMeridian);
        int N = (int) Math.pow(2.0, forwards.size());
        String fmt = "%0" + String.format("%d", forwards.size()) + "d";
        for (int i = 0; i < N; i++) {
            String mask = String.format(fmt, Integer.parseInt(Integer.toBinaryString(i)));
            CompoundTrf v2CompoundCT = new CompoundTrf();
            v2CompoundCT.setTrfName(nameTrf);
            v2CompoundCT.setPolicy("Concatenated");
            List<SingleTrf> sts = new ArrayList<>();
            for (int t = 0; t < forwards.size(); t++) {
                if (mask.substring(t, t + 1).equals("0")) sts.add(forwards.get(t));
                else sts.add(inverses.get(t));
            }
            List<String> code = new ArrayList<>();
            code.add("85");
            for (SingleTrf trf : sts) {
                code.add(trf.getAuthorityCode().getCode().replace("8500", ""));
            }
            v2CompoundCT.setAuthorityCode(new AuthorityCode("Test", String.join("",code)));
            v2CompoundCT.setTransformationList(sts);
            compoundCTs.add(v2CompoundCT);
        }
        return compoundCTs;
    }

    private static Object ITrfToV2Impl(ITrf trf) {
        return PersistableReference.createInstance(trf.createPersistableReference());
    }

    private static Object ILbCrsToV2Impl(ILateBoundCrs crs) {
        return PersistableReference.createInstance(crs.createPersistableReference());
    }

    private static int sourceMeridian(String name) {
        String s = name.replace("G-", "").replace(" to ", "|");
        String[] parts = s.split("\\|");
        return Integer.parseInt(parts[0]);
    }

    private static int targetMeridian(String name) {
        String s = name.replace("G-", "").replace(" to ", "|");
        String[] parts = s.split("\\|");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private static IEarlyBoundCrs makeEarlyBoundCrs(ILateBoundCrs iLbCrs, ITrf iTrf) {
        if (iLbCrs == null || iTrf == null) return null;
        int sourceMeridian = sourceMeridian(iTrf.getName());
        int targetMeridian = targetMeridian(iTrf.getName());

        LateBoundCrs lbCrs = (LateBoundCrs) ILbCrsToV2Impl(iLbCrs);
        Object trf = ITrfToV2Impl(iTrf);
        EarlyBoundCrs ebCrs = new EarlyBoundCrs();
        String name = String.format("%s bound to %s [%s, %s]", iLbCrs.getName(), iTrf.getName(),
                iLbCrs.getAuthorityCode().getCode(), iTrf.getAuthorityCode().getCode());
        ebCrs.setCrsName(name);
        if (trf instanceof SingleTrf) ebCrs.setSingleTransformation((SingleTrf) trf);
        if (trf instanceof CompoundTrf) ebCrs.setCompoundTransformation((CompoundTrf) trf);
        ebCrs.setLateBoundCrs(lbCrs);
        ebCrs.setAuthorityCode(new AuthorityCode("Test", String.format("%d", 900000 + sourceMeridian * 10 + targetMeridian)));
        return (IEarlyBoundCrs) parseSpatialReference(ebCrs.toJsonString());
    }
}
