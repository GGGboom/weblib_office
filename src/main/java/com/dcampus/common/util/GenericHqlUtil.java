package com.dcampus.common.util;

import com.dcampus.common.paging.*;
import java.util.List;


public class GenericHqlUtil {
    public GenericHqlUtil() {
    }

    static String getHql(SearchTerm searchTerm, SortTerm sortTerm, List<Object> list) {
        return getHql((String)null, searchTerm, sortTerm, list);
    }
    static int k=0;
    public static String getHql(String hql, SearchTerm searchTerm, SortTerm sortTerm, List<Object> list) {
        StringBuffer sbuffer = null;
        if (hql != null && hql.length() > 0) {
            sbuffer = new StringBuffer(hql);
        } else {
            sbuffer = new StringBuffer();
        }

        if (searchTerm != null && (searchTerm.getSearchItems().length != 0 || searchTerm.getSearchTerms().length != 0)) {
            sbuffer.append(" where ");
            travelSearchTerm(searchTerm, sbuffer, list);
        }

        if (sortTerm != null) {
            travelSortTerm(sortTerm, sbuffer);
        }
        k=0;
        return sbuffer.toString();
    }

    static String getHql(SearchTerm searchTerm, List<Object> list) {
        return getHql((String)null, (SearchTerm)searchTerm, list);
    }

    public static String getHql(String hql, SearchTerm searchTerm, List<Object> list) {
        StringBuffer sbuffer = null;
        if (hql != null && hql.length() > 0) {
            sbuffer = new StringBuffer(hql);
        } else {
            sbuffer = new StringBuffer();
        }

        if (searchTerm != null && (searchTerm.getSearchItems().length != 0 || searchTerm.getSearchTerms().length != 0)) {
            sbuffer.append(" where ");
            travelSearchTerm(searchTerm, sbuffer, list);
        }
        k=0;
        return sbuffer.toString();
    }

    private static void travelSortTerm(SortTerm sortTerm, StringBuffer sbuffer) {
        SortItem[] sItems = sortTerm.getTerms();
        if (sItems.length > 0) {
            sbuffer.append(" order by ");

            for(int i = 0; i < sItems.length; ++i) {
                sbuffer.append(sItems[i].getKey().getOriginalName());
                sbuffer.append(SortItem.Order.DESC.equals(sItems[i].getOrder()) ? " DESC " : " ASC ");
                if (i < sItems.length - 1) {
                    sbuffer.append(",");
                }
            }
        }

    }

    private static void travelSearchTerm(SearchTerm searchTerm, StringBuffer sbuffer, List<Object> list) {
        SearchTerm.Type type = searchTerm.getType();
        String op = SearchTerm.Type.OR.equals(type) ? " or " : " and ";
        SearchItem[] sItems = searchTerm.getSearchItems();
        SearchTerm[] sTerms = searchTerm.getSearchTerms();
        if (sItems.length != 0 || sTerms.length != 0) {
            int i;
            if (sItems.length > 0) {
                for(i = 0; i < sItems.length; ++i) {
                    comparisonToHql(sItems[i].getKey(), sItems[i].getComparison(), sItems[i].getValue(), sbuffer, list,k);
                    k++;
                    if (i < sItems.length - 1) {
                        sbuffer.append(op);
                    }
                }

                if (sTerms.length > 0) {
                    sbuffer.append(op);

                    for(i = 0; i < sTerms.length; ++i) {
                        sbuffer.append("(");
                        travelSearchTerm(sTerms[i], sbuffer, list);
                        sbuffer.append(")");
                        if (i < sTerms.length - 1) {
                            sbuffer.append(op);
                        }
                    }
                }
            } else {
                for(i = 0; i < sTerms.length; ++i) {
                    sbuffer.append("(");
                    travelSearchTerm(sTerms[i], sbuffer, list);
                    sbuffer.append(")");
                    if (i != sTerms.length - 1) {
                        sbuffer.append(op);
                    }
                }
            }

        }
    }


    private static <T> void comparisonToHql(SearchItemKey<T> key, SearchItem.Comparison comparison, Object value, StringBuffer sbuffer, List<Object> list,int i) {
        sbuffer.append(key.getOriginalName());
        if (key.getValueClass() != null) {
            boolean ignore = false;
            switch((comparison.ordinal()+1)) {
                case 1:
                    sbuffer.append(" <= ?"+(i+1)+" ");
                    break;
                case 2:
                    sbuffer.append(" < ?"+(i+1)+" ");
                    break;
                case 3:
                    if (value == null) {
                        sbuffer.append(" is null ");
                        ignore = true;
                    } else {
                        sbuffer.append(" = ?"+(i+1)+" ");
                    }
                    break;
                case 4:
                    if (value == null) {
                        sbuffer.append(" is not null ");
                        ignore = true;
                    } else {
                        sbuffer.append(" != ?"+(i+1)+" ");
                    }
                    break;
                case 5:
                    sbuffer.append(" > ?"+(i+1)+" ");
                    break;
                case 6:
                    sbuffer.append(" >= ?"+(i+1)+" ");
                    break;
                case 7:
                case 8:
                default:
                    ignore = true;
                    break;
                case 9:
                case 11:
                case 13:
                    sbuffer.append(" like ?"+(i+1)+" ");
                    break;
                case 10:
                case 12:
                case 14:
                    sbuffer.append(" not like ?"+(i+1)+" ");
            }

//            switch((comparison.ordinal()+1)) {
//                case 1:
//                    sbuffer.append(" <= ?"+k+" ");
//                    break;
//                case 2:
//                    sbuffer.append(" < ?"+k+" ");
//                    break;
//                case 3:
//                    if (value == null) {
//                        sbuffer.append(" is null ");
//                        ignore = true;
//                    } else {
//                        sbuffer.append(" = ?"+k+" ");
//                    }
//                    break;
//                case 4:
//                    if (value == null) {
//                        sbuffer.append(" is not null ");
//                        ignore = true;
//                    } else {
//                        sbuffer.append(" != ?"+k+" ");
//                    }
//                    break;
//                case 5:
//                    sbuffer.append(" > ?"+k+" ");
//                    break;
//                case 6:
//                    sbuffer.append(" >= ?"+k+" ");
//                    break;
//                case 7:
//                case 8:
//                default:
//                    ignore = true;
//                    break;
//                case 9:
//                case 11:
//                case 13:
//                    sbuffer.append(" like ?"+k+" ");
//                    break;
//                case 10:
//                case 12:
//                case 14:
//                    sbuffer.append(" not like ?"+k+" ");
//            }

            if (comparison != SearchItem.Comparison.LK && comparison != SearchItem.Comparison.NL) {
                if (comparison != SearchItem.Comparison.SW && comparison != SearchItem.Comparison.NSW) {
                    if (comparison != SearchItem.Comparison.EW && comparison != SearchItem.Comparison.NEW) {
                        if (!ignore) {
                            list.add(value);
                        }
                    } else {
                        list.add("%" + value.toString());
                    }
                } else {
                    list.add(value.toString() + "%");
                }
            } else {
                list.add("%" + value.toString() + "%");
            }

        }
    }
}
