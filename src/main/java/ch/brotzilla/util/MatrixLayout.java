// Created by Lawrence PC Dol.  Released into the public domain.
//
// Source is licensed for any use, provided this copyright notice is retained.
// No warranty for any purpose whatsoever is implied or expressed.  The author
// is not liable for any losses of any kind, direct or indirect, which result
// from the use of this software.

package ch.brotzilla.util;

import java.awt.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

/**
 * A layout manager that is similar in concept (and models behavior from) HTML
 * tables. The layout consists of a grid of cells each of which may contain a
 * component. Components may span multiple cells both across rows and columns.
 * The size of the matrix, in terms of rows and colums is determined at
 * construction.
 * <p>
 * Components are added to the layout using a string of attributes, similar to
 * what is found in the various tags for an HTML table. Allowable attributes are
 * detailed in the following tables.
 * <p>
 * <table nowrap=true>
 * <tr>
 * <td colspan="9">
 * <h3>Row Attributes</h3></td>
 * </tr>
 * <tr>
 * <td bgcolor=#E0E0E0>Attribute</td>
 * <td width=20></td>
 * <td bgcolor=#E0E0E0>Function</td>
 * <td width=20></td>
 * <td bgcolor=#E0E0E0>Default</td>
 * <td width=20></td>
 * <td bgcolor=#E0E0E0>Values</td>
 * <td width=20></td>
 * <td bgcolor=#E0E0E0>Notes</td>
 * </tr>
 * <tr>
 * <td>Size</td>
 * <td></td>
 * <td>Row Height</td>
 * <td></td>
 * <td>"Preferred"</td>
 * <td></td>
 * <td>"Preferred"/"Pref", n!, n.nn%</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>CellSpan</td>
 * <td></td>
 * <td>Default row spanning for cell contents</td>
 * <td></td>
 * <td>Defaults</td>
 * <td></td>
 * <td>1-number of rows, or '*' for all remaining</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>CellAlign</td>
 * <td></td>
 * <td>Default vertical alignment for cell contents</td>
 * <td></td>
 * <td>Defaults</td>
 * <td></td>
 * <td>"Top", "Center" or "Middle", "Bottom", "Fill"</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>CellInsets</td>
 * <td></td>
 * <td>Default vertical insets for cell contents</td>
 * <td></td>
 * <td>Defaults</td>
 * <td></td>
 * <td>top,bottom (each can be Default, Def, or Dft)</td>
 * <td></td>
 * <td>#1</td>
 * </tr>
 * <tr>
 * <td>CellGroup</td>
 * <td></td>
 * <td>Default vertical group for cell contents</td>
 * <td></td>
 * <td>Defaults</td>
 * <td></td>
 * <td>A label to link cells for common heights</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td colspan="9">
 * <p>
 * #1 Insets for the outer edges of cells which are on the outer edges of the
 * container are ignored.</td>
 * </tr>
 * <tr>
 * <td colspan="9">
 * <h3>Column Attributes</h3></td>
 * </tr>
 * <tr>
 * <td bgcolor=#E0E0E0>Attribute</td>
 * <td width=20></td>
 * <td bgcolor=#E0E0E0>Function</td>
 * <td width=20></td>
 * <td bgcolor=#E0E0E0>Default</td>
 * <td width=20></td>
 * <td bgcolor=#E0E0E0>Values</td>
 * <td width=20></td>
 * <td bgcolor=#E0E0E0>Notes</td>
 * </tr>
 * <tr>
 * <td>Size</td>
 * <td></td>
 * <td>Column width</td>
 * <td></td>
 * <td>"Preferred"</td>
 * <td></td>
 * <td>"Preferred"/"Pref", n!, n.nn%</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>CellSpan</td>
 * <td></td>
 * <td>Default column spanning for cell contents</td>
 * <td></td>
 * <td>Defaults</td>
 * <td></td>
 * <td>1-number of columns, or '*' for all remaining</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>CellAlign</td>
 * <td></td>
 * <td>Default horizontal alignment for cell contents</td>
 * <td></td>
 * <td>Defaults</td>
 * <td></td>
 * <td>"Left", "Center" or "Middle", "Right", "Fill"</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>CellInsets</td>
 * <td></td>
 * <td>Default horizontal insets for cell contents</td>
 * <td></td>
 * <td>Defaults</td>
 * <td></td>
 * <td>left,right (each can be Default, Def, or Dft)</td>
 * <td></td>
 * <td>#1</td>
 * </tr>
 * <tr>
 * <td>CellGroup</td>
 * <td></td>
 * <td>Default horizontal group for cell contents</td>
 * <td></td>
 * <td>Defaults</td>
 * <td></td>
 * <td>A label to link cells for common widths</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td colspan="9">
 * <p>
 * #1 Insets for the outer edges of cells which are on the outer edges of the
 * container are ignored.</td>
 * </tr>
 * <tr>
 * <td colspan="9">
 * <h3>Cell Attributes</h3></td>
 * </tr>
 * <tr>
 * <td bgcolor=#E0E0E0>Attribute</td>
 * <td width=20></td>
 * <td bgcolor=#E0E0E0>Function</td>
 * <td width=20></td>
 * <td bgcolor=#E0E0E0>Default</td>
 * <td width=20></td>
 * <td bgcolor=#E0E0E0>Values</td>
 * <td width=20></td>
 * <td bgcolor=#E0E0E0>Notes</td>
 * </tr>
 * <tr>
 * <td>Row</td>
 * <td></td>
 * <td>Row number</td>
 * <td></td>
 * <td>"Current"</td>
 * <td></td>
 * <td>"Next", "Current", 1 - Number of rows</td>
 * <td></td>
 * <td>#1</td>
 * </tr>
 * <tr>
 * <td>Col</td>
 * <td></td>
 * <td>Column number</td>
 * <td></td>
 * <td>"Next"</td>
 * <td></td>
 * <td>"Next", "Current", 1 - Number of columns</td>
 * <td></td>
 * <td>#1</td>
 * </tr>
 * <tr>
 * <td>hSpan</td>
 * <td></td>
 * <td>Number of columns to span</td>
 * <td></td>
 * <td>"1"</td>
 * <td></td>
 * <td>1 - Number of Columns, or '*' for all remaining</td>
 * <td></td>
 * <td>#2</td>
 * </tr>
 * <tr>
 * <td>vSpan</td>
 * <td></td>
 * <td>Number of rows to span</td>
 * <td></td>
 * <td>"1"</td>
 * <td></td>
 * <td>1 - Number of Rows, or '*' for all remaining</td>
 * <td></td>
 * <td>#2</td>
 * </tr>
 * <tr>
 * <td>hAlign</td>
 * <td></td>
 * <td>Horizontal alignment of component in cell</td>
 * <td></td>
 * <td>"Left"</td>
 * <td></td>
 * <td>"Left", "Center" or "Middle", "Right", "Fill"</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>vAlign</td>
 * <td></td>
 * <td>Vertical alignment of component in cell</td>
 * <td></td>
 * <td>"Middle"</td>
 * <td></td>
 * <td>"Top", "Center" or "Middle", "Bottom", "Fill"</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>hGroup</td>
 * <td></td>
 * <td>Horizontal cell-group name</td>
 * <td></td>
 * <td><i>None</i></td>
 * <td></td>
 * <td>A label to link cells for common widths</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>vGroup</td>
 * <td></td>
 * <td>Vertical cell-group name</td>
 * <td></td>
 * <td><i>None</i></td>
 * <td></td>
 * <td>A label to link cells for common heights</td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>Insets</td>
 * <td></td>
 * <td>Insets for cell</td>
 * <td></td>
 * <td>"3,3:3,3"</td>
 * <td></td>
 * <td>top,left:bottom,right (each can be "Default,Def, or Dft")</td>
 * <td></td>
 * <td>#3</td>
 * </tr>
 * <tr>
 * <td colspan="9">
 * #1 Both row and column may not be "Current" since only 1 component is allowed
 * in each cell.<br />
 * #2 The number of cells to span cannot extend beyond to the last cell in the
 * row or column.<br />
 * #3 Insets for the outer edges of cells which are on the outer edges of the
 * container are ignored.<br />
 * </td>
 * </tr>
 * </table>
 * 
 * <p>
 * The cell defaults provided by individual rows or columns apply to vertically
 * rows and horizontally to columns. Defaulting is done at two levels. If the
 * cell does not specify a particular attribute, then it is taken from the row
 * (for a vertical attribute) or the column (for a horizontal attribute). If the
 * row or column does not specify a cell default for that value, then it is
 * taken from the layout cell defaults. The default values listed above are used
 * if neither of the two defaulting layers has a value.
 * 
 * <h3>How Size Is Calculated</h3>
 * Size is calculated whenever a container with a MatrixLayout is asked for its
 * preferred or minimum size. Rows have height, columns have width and the size
 * of any given cell is determined by its row and column. Note that the logic
 * for calulating a row's height is the identical as that for calculating a
 * column's width, but using the components height rather than width.
 * <p>
 * The logic employed is simple and predictable:
 * <ol>
 * <li>All sizes are reset.
 * <li>Rows/columns with fixed height/width are set.
 * <li>Rows/columns with preferred height/width are set to the largest preferred
 * height/width of all cells in that row/column that don't span.
 * <li>Rows/columns with variable height/width are allocated so that all
 * rows/columns are the correct ratio, and all cells that don't span are at
 * least their preferred height/width.
 * <li>Finally, variable rows/columns are expanded so that all cells that span
 * are at least their preferred height/width. Note that if no variable
 * rows/columns are specified, spanning components may not achieve their
 * preferred size.
 * </ol>
 * <h3>How Space Is Allocated</h3> Space is allocated whenever the container is
 * layed out.
 * <p>
 * The logic employed is simple and predictable:
 * <ol>
 * <li>All sizes are reset.
 * <li>Rows/columns with fixed height/width are set.
 * <li>Rows/columns with preferred height/width are set to the largest preferred
 * height/width of all cells in that rows/column that don't span.
 * <li>Remaining space is assigned to variable rows/columns in the proportions
 * specified with the last allocation being for all remaining to absorb
 * accumulation of decimal remainders (fractional parts of pixels dropped
 * because heights/widths are a integer values).
 * </ol>
 * <p>
 * <h3>Example - Form with labels, fields and a button bar</h3>
 * 
 * <pre>
 * +--------------------------------------------------------------------------------+
 * +                                                                                |
 * +     Name : |________________________________________________________________|  |
 * +                                                                                |
 * +  Address : |________________________________________________________________|  |
 * +                                                                                |
 * +            |________________________________________________________________|  |
 * +                                                                                |
 * +            |________________________________________________________________|  |
 * +                                                                                |
 * +     City : |____________________| State |__| Zip |_____| - |____|              |
 * +                                                                                |
 * +    Phone : |___|-|___|-|____|                                                  |
 * +                                                                                |
 * +    Notes : |                              |  |                              |  |
 * +            |                              |  |                              |  |
 * +            |                              |  |                              |  |
 * +            |                              |  |                              |  |
 * +            |                              |  |                              |  |
 * +            |______________________________|  |______________________________|  |
 * +            [BOTTOM-LEFT]                                     [ BOTTOM-RIGHT ]  |
 * +                                                                                |
 * +                       [      Yes      ]   [      No      ]   [    Abort     ]  |
 * +                                                                                |
 * +--------------------------------------------------------------------------------+
 * 
 * private void createContent(Container main){
 *     String[]                            rows,cols;                                               // row/column specification arrays
 * 
 *     JPanel                              phnpnl,cszpnl,btnpnl;                                    // special nested panels
 * 
 *     name        =createField ("",50);
 *     address1    =createField ("",50);
 *     address2    =createField ("",40);
 *     address3    =createField ("",40);
 *     city        =createField ("",20);
 *     state       =createField ("", 3);
 *     zip         =createField ("", 5);
 *     zipext      =createField ("", 4);
 *     phnPart1    =createField ("", 3);
 *     phnPart2    =createField ("", 3);
 *     phnPart3    =createField ("", 4);
 *     notes1      =createField ("",20,5);
 *     notes2      =createField ("",20,5);
 *     notes3      =createButton("Bottom Left" ,"ACTION1");
 *     notes4      =createButton("Bottom Right","ACTION2");
 * 
 *     yes         =createButton("Yes"   ,"YES");    yes   .addEventHandler(DctEvent.Events.ACN_COMMAND,this);
 *     no          =createButton("No"    ,"NO");     no    .addEventHandler(DctEvent.Events.ACN_COMMAND,this);
 *     cancel      =createButton("Cancel","CANCEL"); cancel.addEventHandler(DctEvent.Events.ACN_COMMAND,this);
 * 
 *     // CREATE MAIN PANEL WITH DESIRED ROWS AND COLUMNS
 *     rows=MatrixLayout.arrayOf(10,"Size=Pref CellAlign=Middle CellInsets=5,0");                   // standard row spec
 *     rows[6]                     ="Size=100% CellAlign=Top    CellInsets=5,0";                    // note: row 7 ([6] is index)
 *     rows[7]                     ="Size=Pref CellAlign=Top    CellInsets=5,0";                    // note: row 8 ([7] is index)
 *     rows[8]                     ="Size=Pref CellAlign=Top    CellInsets=5,0";                    // note: row 9 ([8] is index)
 *     cols=MatrixLayout.arrayOf(3 ,"size=Pref CellAlign=Right  CellInsets=5,0");                   // standard column spec
 *     cols[1]                     ="Size=50%  CellAlign=Left   CellInsets=5,0";                    // note: col 2 ([1] is index)
 *     cols[2]                     ="Size=50%  CellAlign=Left   CellInsets=5,0";                    // note: col 3 ([2] is index)
 *     con.setLayout(new MatrixLayout(rows,cols,"Row=Cur Col=Next"));
 * 
 *     // CREATE SPECIAL NESTED PANELS
 *     phnpnl=MatrixLayout.singleRowBar(5,false,new DctComponent[]{phnPart1,phnPart2,phnPart3                                   });
 *     cszpnl=MatrixLayout.singleRowBar(5,1    ,new DctComponent[]{city,createLabel("State"),state,createLabel("Zip"),zip,zipext});
 *     btnpnl=MatrixLayout.singleRowBar(5,true ,new DctComponent[]{yes,no,cancel                                                });
 *     phnpnl.setName("PhonePanel");
 *     cszpnl.setName("CityStateZipPanel");
 *     btnpnl.setName("ButtonPanel");
 * 
 *     // ADD COMPONENTS TO MAIN PANEL
 *     con.add(createLabel(   "Name :"),"row=Next col=1"); con.add(name    ,"               hAlign=Fill  hSpan=2                               ");
 *     con.add(createLabel("Address :"),"row=Next col=1"); con.add(address1,"               hAlign=Fill  hSpan=2                               ");
 *                                                         con.add(address2,"Row=Next Col=2 hAlign=Fill  hSpan=2                               ");
 *                                                         con.add(address3,"Row=Next Col=2 hAlign=Fill  hSpan=2                               ");
 *     con.add(createLabel(   "City :"),"row=Next col=1"); con.add(cszpnl  ,"                            hSpan=2                               ");
 *     con.add(createLabel(  "Phone :"),"row=Next col=1"); con.add(phnpnl  ,"                            hSpan=2                               ");
 *     con.add(createLabel(  "Notes :"),"row=Next col=1"); con.add(notes1  ,"Row=Cur  Col=2 hAlign=Fill          vAlign=Fill                   ");
 *                                                         con.add(notes2  ,"Row=Cur        hAlign=Fill          vAlign=Fill                   ");
 *                                                         con.add(notes3  ,"Row=Next Col=2 hAlign=Left                      hGroup=NoteButtons");
 *                                                         con.add(notes4  ,"Row=Cur        hAlign=Right                     hGroup=NoteButtons");
 *     con.add(btnpnl                  ,"row=Next col=1 hAlign=Right hSpan=3");
 *     main.setBorder(new DctEmptyBorder(10));
 *     main.setBackground(SystemColor.window);
 *     }
 * </pre>
 */

public class MatrixLayout extends Object implements LayoutManager2 {

    // *************************************************************************************************
    // INSTANCE PROPERTIES
    // *************************************************************************************************

    private RowCol[] rows; // rows in this table
    private RowCol[] cols; // columns in this table
    private CellConstraints defaults; // cell defaults

    private Component[][] components; // components in each cell of the table.
    private CellConstraints[][] constraints; // constraints for each component.
    private CellConstraints lastConstraints; // for next/current row and column
    private int[] rowSizes; // calculated sizes for each row in the table.
    private int[] colSizes; // calculated sizes for each column in the table.

    // LAYOUT CACHE OBJECTS
    private boolean layingOut; // whether we are laying out the container
    private Dimension cSize; // cached container size.
    private Dimension pSize; // cached preferred size.
    private Dimension mSize; // cached minimum size.

    // *************************************************************************************************
    // INSTANCE CONSTRUCTION/INITIALIZATON/FINALIZATION, OPEN/CLOSE
    // *************************************************************************************************

    /**
     * Create a Matrix Layout from an abbreviated list of sizes. This consists
     * of a string of space separated "size" attribute values, as defined in the
     * class level help. Just the value is used, not the keyword, e.g.:
     * "10% Preferred 50% Preferred 40% 100". One size value is required for
     * each row and column in the matrix.
     */
    public MatrixLayout(String rows, String cols) {
        this(rows, cols, null);
    }

    /**
     * Create a Matrix Layout from an abbreviated list of sizes, with a set of
     * cell defaults. This consists of a string of space separated "size"
     * attribute values, as defined in the class level help. Just the value is
     * used, not the keyword, e.g.: "10% Preferred 50% Preferred 40% 100".
     */
    public MatrixLayout(String rows, String cols, String dfts) {
        super();

        /* +J5 */ArrayList<String> rw; // rows
        /* +J5 */ArrayList<String> cl; // columns
        // -J5*/ArrayList rw; // rows
        // -J5*/ArrayList cl; // columns
        StringTokenizer st; // string tokenizer

        /* +J5 */rw = new ArrayList<String>();
        /* +J5 */cl = new ArrayList<String>();
        // -J5*/rw=new ArrayList();
        // -J5*/cl=new ArrayList();

        st = new StringTokenizer(rows, " \t");
        while (st.hasMoreTokens()) {
            rw.add("size=" + st.nextToken());
        }

        st = new StringTokenizer(cols, " \t");
        while (st.hasMoreTokens()) {
            cl.add("size=" + st.nextToken());
        }

        init(rw.toArray(new String[rw.size()]), cl.toArray(new String[cl.size()]), dfts);
    }

    /**
     * Create a Matrix Layout from a detailed array of row and column
     * specifications. A specification is a string of space separated
     * keyword/value pairs as defined in the class level help. One specification
     * is required for each row and column in the matrix.
     */
    public MatrixLayout(String[] rows, String[] cols) {
        this(rows, cols, null);
    }

    /**
     * Create a Matrix Layout from a detailed array of row and column
     * specifications with a set of cell defaults. A specification is a string
     * of space separated keyword/value pairs as defined in the class level
     * help. One specification is required for each row and column in the
     * matrix.
     */
    public MatrixLayout(String[] rows, String[] cols, String dfts) {
        super();

        init(rows, cols, dfts);
    }

    private void init(String[] rows, String[] cols, String dfts) {
        int tp;

        tp = 0;
        this.rows = new RowCol[rows.length];
        for (int xa = 0; xa < rows.length; xa++) {
//            if (diags) {
//                log.println("Input    R" + (xa + 1) + ": " + rows[xa]);
//            }
            this.rows[xa] = new RowCol(rows[xa]);
//            if (diags) {
//                log.println("Resolved R" + (xa + 1) + ": " + this.rows[xa]);
//            }
            if (this.rows[xa].sizeType == '%') {
                tp += this.rows[xa].size;
            }
        }
        if (tp != 0 && tp != 10000) {
            throw new Escape("Percentages used in row but total of all percentages is not exactly 100% (Total=" + pptToString(tp) + ")");
        }

        tp = 0;
        this.cols = new RowCol[cols.length];
        for (int xa = 0; xa < cols.length; xa++) {
//            if (diags) {
//                log.println("Input    C" + (xa + 1) + ": " + cols[xa]);
//            }
            this.cols[xa] = new RowCol(cols[xa]);
//            if (diags) {
//                log.println("Resolved C" + (xa + 1) + ": " + this.cols[xa]);
//            }
            if (this.cols[xa].sizeType == '%') {
                tp += this.cols[xa].size;
            }
        }
        if (tp != 0 && tp != 10000) {
            throw new Escape("Percentages used in column but total of all percentages is not exactly 100% (Total=" + pptToString(tp) + ")");
        }

        defaults = new CellConstraints(dfts);
        defaults.resolveDefaults(); // does not try to use row/col defaults or
                                    // itself!
//        if (diags) {
//            log.println("Cell Defaults: " + defaults);
//        }

        components = new Component[rows.length][cols.length];
        constraints = new CellConstraints[rows.length][cols.length];
        rowSizes = new int[rows.length];
        colSizes = new int[cols.length];

        layingOut = false;
        cSize = null;
        pSize = null;
        mSize = null;
        lastConstraints = null;
    }

    // *************************************************************************************************
    // INSTANCE METHODS - ACCESSORS
    // *************************************************************************************************

    /**
     * Set the specification for a row in the layout. Using this method allows
     * you to create a layout using arrayOf() to set all rows the same and then
     * override a few of them after the fact.
     */
    public synchronized void setRowSpec(int row, String spec) {
        if (row < 1 || row > rows.length) {
            throw new Escape("Index out of bounds setting row specification - value " + row + " is not between 1 and " + rows.length);
        }
        rows[row - 1] = new RowCol(spec);
    }

    /**
     * Set the specification for a column in the layout. Using this method
     * allows you to create a layout using arrayOf() to set all columns the same
     * and then override a few of them after the fact.
     */
    public synchronized void setColSpec(int col, String spec) {
        if (col < 1 || col > cols.length) {
            throw new Escape("Index out of bounds setting column specification - value " + col + " is not between 1 and " + cols.length);
        }
        cols[col - 1] = new RowCol(spec);
    }

    /**
     * Get the component at the specified row and col. If row or col are out of
     * bounds an ArrayOutOfBoundsException is thrown. Row and column are 1+
     * numbers, not offsets.
     * 
     * @param row
     *            The component's row.
     * @param col
     *            The component's column.
     */
    public synchronized Component getComponentAt(int row, int col) {
        if (row < 1 || row > components.length) {
            throw new ArrayIndexOutOfBoundsException("Component row of " + row + " for MatrixLayout is not permitted - must be 1-" + components.length);
        }
        if (col < 1 || col > components[0].length) {
            throw new ArrayIndexOutOfBoundsException("Component column of " + col + " for MatrixLayout is not permitted - must be 1-" + components[0].length);
        }
        return components[row - 1][col - 1];
    }

    public void dump(PrintWriter wtr) {
        wtr.println("Dump Of Matrix Layout");
        wtr.println("  Rows:");
        for (int rr = 0; rr < rows.length; rr++) {
            wtr.println("    - R" + (rr + 1) + ": " + rows[rr]);
        }
        wtr.println("  Columns:");
        for (int cc = 0; cc < cols.length; cc++) {
            wtr.println("    - C" + (cc + 1) + ": " + cols[cc]);
        }
        wtr.println("  Cells:");
        for (int rr = 0; rr < rows.length; rr++) {
            for (int cc = 0; cc < cols.length; cc++) {
                Component cm = components[rr][cc];
                CellConstraints cs = constraints[rr][cc];
                if (cm != null) {
                    wtr.println("    - R" + (rr + 1) + ",C" + (cc + 1) + ": " + cm.getName() + "=" + cs);
                } else {
                    wtr.println("    - R" + (rr + 1) + ",C" + (cc + 1) + ": spanned");
                }
            }
        }
    }

    // *************************************************************************************************
    // INSTANCE METHODS - LAYOUT MANAGER 2 IMPLEMENTATION METHODS
    // *************************************************************************************************

    /**
     * Adds a component to the layout, using <code>cnsobj</code> for the
     * constraint information.
     * 
     * @param com
     *            The component to add
     * @param cnsobj
     *            The constraint object - a string of attribute values in the
     *            form <code>keyword=value</code>. See the class description for
     *            detailed information.
     * @throws IllegalArgumentException
     *             If cnsobj is null or not a String.
     */
    public synchronized void addLayoutComponent(Component com, Object cnsobj) {
        CellConstraints co, cos; // constraint object, spanning constraint
                                 // object

        if (cnsobj != null && !(cnsobj instanceof String)) {
            throw new Escape("Constraints object for MatrixLayout must be a String of attribute=value pairs.");
        }

        co = new CellConstraints((String) cnsobj);
        co.resolveConstraints(this);
        lastConstraints = co; // for Next/Current

        if (com != NULL) {
//            if (diags) {
//                log.println(co);
//            }

            // PROCESS SPANNED CELLS
            cos = null;
            for (int rr = co.row; rr < (co.row + co.vSpan); rr++) {
                for (int cc = co.col; cc < (co.col + co.hSpan); cc++) {
                    if (components[rr][cc] != null) {
                        throw new Escape("Matrix cell (R" + (rr + 1) + ",C" + (cc + 1) + ") already contains a component - cannot add " + com + " (Existing component: " + components[rr][cc] + ")");
                    }
                    cos = new CellConstraints((String) cnsobj);
                    cos.row = (rr + 1);
                    cos.vSpan = 1;
                    cos.col = (cc + 1);
                    cos.hSpan = 1;
                    cos.resolveConstraints(this);
                    components[rr][cc] = null; // to flag any attempt to put a
                                               // component into a spanned cell
                    constraints[rr][cc] = cos; // only needed for next
                                               // processing in
                                               // resolveDefaults()
                }
            }
            if (cos != null) { // so layout doesn't have to search out the end
                               // cell constraints
                co.vInsets[1] = cos.vInsets[1];
                co.hInsets[1] = cos.hInsets[1];
            }

            // SET THE PRIMARY CELL (MIGHT BE TO SPANNED)
            components[co.row][co.col] = com;
            constraints[co.row][co.col] = co;
            cSize = null;
            pSize = null;
            mSize = null;
        }
    }

    /**
     * Adds a component to the layout, using <code>cnsstr</code> for the
     * constraint information.
     * 
     * @param cnsstr
     *            The constraint string - attribute values in the form
     *            <code>keyword=value</code>. See the class description for
     *            detailed information.
     * @param com
     *            The component to add
     * @throws IllegalArgumentException
     *             If cnsobj is null or not a String.
     * @deprecated Use
     *             <code>addLayoutComponent(Component com, Object cnsobj)</code>
     *             instead.
     */
    /* +J5 */@Deprecated
    public synchronized void addLayoutComponent(String cnsstr, Component com) {
        addLayoutComponent(com, cnsstr);
    }

    /**
     * Remove a component from the layout.
     */
    public synchronized void removeLayoutComponent(Component com) {
        if (com != null && com != NULL) { // NULL allows the programmer to skip
                                          // cells still using Next/Current
            for (int xa = 0; xa < rows.length; xa++) {
                for (int xb = 0; xb < cols.length; xb++) {
                    if (components[xa][xb] == com) {
                        CellConstraints co = constraints[xa][xb];
                        lastConstraints = co;
                        for (int rr = co.row; rr < (co.row + co.vSpan); rr++) {
                            for (int cc = co.col; cc < (co.col + co.hSpan); cc++) {
                                components[rr][cc] = null;
                                constraints[rr][cc] = null;
                            }
                        }
                    }
                }
            }
        }

        cSize = null;
        pSize = null;
        mSize = null;
    }

    /**
     * Invalidate any cached layout information. Calling this will cause
     * MatrixLayout to recalculate preferred and current sizes.
     */
    public synchronized void invalidateLayout(Container con) {
        if (!layingOut) {
            cSize = null;
            pSize = null;
            mSize = null;
        }
    }

    /**
     * Returns the mimimum layout size. See class level help for details.
     */
    public synchronized Dimension minimumLayoutSize(Container con) {
        if (mSize == null) {
            mSize = new Dimension(calculatePrefOrMinDimensions(con.getInsets(), null, null, false));
        }
        return new Dimension(mSize);
    }

    /**
     * Returns the preferred layout size. See class level help for details.
     */
    public synchronized Dimension preferredLayoutSize(Container con) {
        if (pSize == null) {
            pSize = new Dimension(calculatePrefOrMinDimensions(con.getInsets(), null, null, true));
        }
        return new Dimension(pSize);
    }

    /**
     * Returns maximum layout size. Currently MatrixLayout just returns
     * MAX_VALUE,MAX_VALUE.
     */
    public synchronized Dimension maximumLayoutSize(Container con) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Returns layout X alignment.
     */
    public synchronized float getLayoutAlignmentX(Container con) {
        return 0;
    }

    /**
     * Returns layout Y alignment.
     */
    public synchronized float getLayoutAlignmentY(Container con) {
        return 0;
    }

    /**
     * Lay out the components in the container. See class level help for
     * details.
     */
    public synchronized void layoutContainer(Container con) {
        Insets ins; // container's insets

        // A bug (?) in the J9 VM invokes invalidate() during layoutContainer()
        // when the component is resized.
        // The layingOut flag is specifically to be able to ignore this
        // incorrect reentrant call.

        layingOut = true;
        try {
//            if (diags) {
//                log.println("Layout Container " + con.getName() + ": " + con.getSize() + " :: " + con.getInsets());
//            }

            setSizesForContainer(con);

            ins = con.getInsets();
            if (ins == null) {
                ins = new Insets(0, 0, 0, 0);
            }
            for (int rr = 0, yo = ins.top; rr < rows.length; yo += rowSizes[rr], rr++) {
                for (int cc = 0, xo = ins.left; cc < cols.length; xo += colSizes[cc], cc++) {
                    Component cm = components[rr][cc]; // component
                    CellConstraints cs = constraints[rr][cc]; // constraints
                    Dimension ps, xs; // component preferred size, maximum size
                    int lf, tp, wd, ht; // left, top, width, height
                    int sr, sb, sw, sh; // spanning: right, bottom, width,
                                        // height

                    // ENSURE CURRENT CELL HAS SOMETHING TO DISPLAY
                    if (!isOccupied(cm, cs)) {
//                        if (diags) {
//                            log.println("Cell (R" + (rr + 1) + ",C" + (cc + 1) + ") of " + con.getClass() + " skipped - cell is " + (cm == null ? "spanned" : "empty"));
//                        }
                        continue;
                    }
//                    if (diags) {
//                        log.println("Cell (R" + (rr + 1) + ",C" + (cc + 1) + ") of " + con.getClass() + " :: " + cm.getClass() + " :: " + cs);
//                    }

                    // CALCULATE MAX WIDTH/HEIGHT AVAILABLE TO THE COMPONENT
                    sw = colSizes[cc];
                    sr = (cc + cs.hSpan - 1);
                    for (int xx = (cc + 1); xx <= sr; xx++) {
                        sw += colSizes[xx];
                    }
                    sh = rowSizes[rr];
                    sb = (rr + cs.vSpan - 1);
                    for (int xx = (rr + 1); xx <= sb; xx++) {
                        sh += rowSizes[xx];
                    }

                    // GET SIZES, GIVEN MAX WIDTH AND HEIGHT (NOT INSETS
                    // ADJUSTED BECAUSE THE COMPONENT ACCOUNTS FOR IT'S INSETS
                    // IN CALCULATING THESE)
                    ps = getPrefOrMinDimension(cm, cs, true);
                    xs = cm.getMaximumSize();
                    if (xs.width <= ps.width) {
                        xs.width = 1000000;
                    } // swing/awt allow maximum dimensions to be less than
                      // preferred
                    if (xs.height <= ps.height) {
                        xs.height = 1000000;
                    } // swing/awt allow maximum dimensions to be less than
                      // preferred

                    // CALCULATE THE LEFT & WIDTH (INSETS ADJUSTED)
                    lf = xo;
                    if (cc != 0) {
                        sw -= cs.hInsets[0];
                        lf += cs.hInsets[0];
                    }
                    if (sr != (cols.length - 1)) {
                        sw -= cs.hInsets[1];
                    }
                    switch (cs.hAlign) {
                    case LEFT_TOP: {
                        wd = Math.min(Math.min(sw, ps.width), xs.width);
                    }
                        break;
                    case MIDDLE: {
                        wd = Math.min(Math.min(sw, ps.width), xs.width);
                        lf += ((sw - wd) / 2);
                    }
                        break;
                    case RIGHT_BOTTOM: {
                        wd = Math.min(Math.min(sw, ps.width), xs.width);
                        lf += (sw - wd);
                    }
                        break;
                    case FILL: {
                        wd = sw;/* Math.min(sw,xs.width); */
                    }
                        break;
                    default: {
                        throw new Escape("CODING MISTAKE - Bad hAlign value");
                    }
                    }
                    if (lf < ins.left) {
                        lf = ins.left;
                        wd -= (ins.left - lf);
                    }
                    if (wd > (cSize.width - ins.right - lf)) {
                        wd = (cSize.width - ins.right - lf);
                    }

                    // CALCULATE THE TOP & HEIGHT (INSETS ADJUSTED)
                    tp = yo;
                    if (rr != 0) {
                        sh -= cs.vInsets[0];
                        tp += cs.vInsets[0];
                    }
                    if (sb != (rows.length - 1)) {
                        sh -= cs.vInsets[1];
                    }
                    switch (cs.vAlign) {
                    case LEFT_TOP: {
                        ht = Math.min(Math.min(sh, ps.height), xs.height);
                    }
                        break;
                    case MIDDLE: {
                        ht = Math.min(Math.min(sh, ps.height), xs.height);
                        tp += ((sh - ht) / 2);
                    }
                        break;
                    case RIGHT_BOTTOM: {
                        ht = Math.min(Math.min(sh, ps.height), xs.height);
                        tp += (sh - ht);
                    }
                        break;
                    case FILL: {
                        ht = sh;/* Math.min(sh,xs.height); */
                    }
                        break;
                    default: {
                        throw new Escape("CODING MISTAKE - Bad vAlign value");
                    }
                    }
                    if (tp < ins.top) {
                        tp = ins.top;
                        ht -= (ins.top - tp);
                    }
                    if (ht > (cSize.height - ins.bottom - tp)) {
                        ht = (cSize.height - ins.bottom - tp);
                    }

                    // LAYOUT COMPONENT
//                    if (diags) {
//                        log.println("Component located: Left=" + lf + ", Top=" + tp + ", Width=" + wd + ", Height=" + ht);
//                    }
                    cm.setLocation(lf, tp);
                    cm.setSize(wd, ht);
                }
            }
        } finally {
            layingOut = false;
        }
    }

    // *************************************************************************************************
    // INSTANCE METHODS - SIZE CALCULATION
    // *************************************************************************************************

    private void setSizesForContainer(Container con) {
        Insets in = con.getInsets(); // container's insets

        if (cSize != null && cSize.width == con.getWidth() && cSize.height == con.getHeight()) {
            return;
        }

        pSize = preferredLayoutSize(con); // ensure pSize is set
        if (cSize == null) {
            cSize = new Dimension();
        }

        if (cSize.height != con.getHeight()) {
            cSize.height = calculateRowOrColSize(con.getHeight(), (in.top + in.bottom), rows, rowSizes, false, true);
            if (cSize.height < pSize.height) { // container is not as tall as it
                                               // would prefer
                int nh = calculateRowOrColSize(-1, (in.top + in.bottom), rows, rowSizes, false, true); // calc
                                                                                                       // preferred
                shrinkRowsOrColumns(rowSizes, rows, cSize.height, nh); // shrink
                                                                       // variable
                                                                       // rows
                                                                       // to not
                                                                       // less
                                                                       // than
                                                                       // minimums
            }
        }

        if (cSize.width != con.getWidth()) {
            cSize.width = calculateRowOrColSize(con.getWidth(), (in.left + in.right), cols, colSizes, true, true);
            if (cSize.width < pSize.width) { // container is not as wide as it
                                             // would prefer
                int nw = calculateRowOrColSize(-1, (in.left + in.right), cols, colSizes, true, true); // calc
                                                                                                      // preferred
                shrinkRowsOrColumns(colSizes, cols, cSize.width, nw); // shrink
                                                                      // variable
                                                                      // columns
                                                                      // to not
                                                                      // less
                                                                      // than
                                                                      // minimums
            }
        }

//        if (diags) {
//            log.println("Container Height: " + cSize.height);
//        }
        for (int xa = 0; xa < rowSizes.length; xa++) {
//            if (diags) {
//                log.println("  R" + (xa < 9 ? "0" : "") + (xa + 1) + "=" + rowSizes[xa]);
//            }
            if (rowSizes[xa] < 0) {
                rowSizes[xa] = 0;
            }
        }
//        if (diags) {
//            log.println("  --");
//        }

//        if (diags) {
//            log.println("Container Width: " + cSize.width);
//        }
        for (int xa = 0; xa < colSizes.length; xa++) {
//            if (diags) {
//                log.println("  C" + (xa < 9 ? "0" : "") + (xa + 1) + "=" + colSizes[xa]);
//            }
            if (colSizes[xa] < 0) {
                colSizes[xa] = 0;
            }
        }
//        if (diags) {
//            log.println("  --");
//        }
    }

    private void shrinkRowsOrColumns(int[] sa, RowCol[] rca, int tgt, int cur) {
        if (tgt < 0 || cur <= tgt) {
            return;
        }

        int[] min = new int[sa.length]; // minimum sizes
        int[] isa = new int[sa.length]; // idividual space available to reduce
                                        // (in % rows/cols)
        int tsa = 0; // total space available to reduce (in % rows/cols)

//        if (diags) {
//            log.println("Shrinking " + (rca == rows ? "rows" : "columns") + " from " + cur + " to " + tgt);
//        }

        for (int xa = 0; xa < sa.length; xa++) {
            if (rca == rows) {
                min[xa] = getPrefOrMinHeightOfRow(xa, false);
            } else {
                min[xa] = getPrefOrMinWidthOfCol(xa, false);
            }
            if (rca[xa].sizeType != '%') {
                isa[xa] = 0;
            } else if ((isa[xa] = (sa[xa] - min[xa])) < 1) {
                isa[xa] = 0;
            } else {
                tsa += isa[xa];
            }
        }

        if (tsa > 0) {
            int trr = (cur - tgt); // total reduction required
            int rmn = trr; // amount remaining to achieve target

            // SHRINK PERCENTAGE OF TOTAL AVAILABLE SPACE FROM EACH
            for (int xa = 0; xa < sa.length && rmn > 0; xa++) {
                if (isa[xa] == 0) {
                    continue;
                }

                int amt = calculatePercentage(trr, (int) (((isa[xa] * 10000L) / tsa)));
                if (amt > isa[xa]) {
                    amt = isa[xa];
                }
                if (amt > rmn) {
                    amt = rmn;
                }
                rmn -= amt;
                cur -= amt;
                sa[xa] -= amt;
                isa[xa] -= amt;
            }

            // TRIM OFF ROUNDING ERRORS
            while (rmn > 0) {
                boolean one = false;
                for (int xa = 0; xa < sa.length && rmn > 0; xa++) {
                    if (isa[xa] == 0) {
                        continue;
                    }

                    rmn--;
                    cur--;
                    sa[xa]--;
                    isa[xa]--;
                    one = true;
                }
                if (!one) {
                    break;
                }
            }
        }

        // SHRINK ALL REMAINING, LARGEST FIRST
        if ((cur = shrinkLargestFirst(cur, tgt, sa, min)) > tgt) {
            shrinkLargestFirst(cur, tgt, sa);
        }
    }

    private int shrinkLargestFirst(int cur, int tgt, int[] siz) {
        return shrinkLargestFirst(cur, tgt, siz, null);
    }

    private int shrinkLargestFirst(int cur, int tgt, int[] siz, int[] min) {
        if (tgt < 0 || cur <= tgt) {
            return cur;
        }

        int avl = 0; // available space to reduce
        int thr = 0; // threshold

        // TOTAL UP AVAILABLE SPACE, FIND LARGEST SIZE (I.E. THE "THRESHOLD")
        // AND ALSO REDUCE ANY REALLY EXCESSIVE SIZE RIGHT OFF THE BAT
        for (int xa = 0; xa < siz.length; xa++) {
            if (siz[xa] > tgt) { // can't have any larger than the overall
                                 // target
                int amt = (siz[xa] - tgt);
                cur -= amt;
                siz[xa] = tgt;
            }
            avl += siz[xa];
            if (siz[xa] > thr) {
                thr = siz[xa];
            }
        }

        // REDUCE EACH SIZE ONE PIXEL AT A TIME UNTIL NO MORE LEFT TO DO, OR
        // TARGET IS ACHIEVED
        boolean one = true;
        avl = Math.min(avl, (cur - tgt));
        while (avl > 0 && one) {
            thr--;
            one = false;
            for (int xa = 0; xa < siz.length && avl > 0; xa++) {
                if (thr < siz[xa] && (min == null || min[xa] < thr)) {
                    siz[xa]--;
                    avl--;
                    cur--;
                    one = true;
                }
            }
        }

        return cur;
    }

    private Dimension calculatePrefOrMinDimensions(Insets ins, int[] rsa, int[] csa, boolean pref) {
        int hgt, wid; // width and height

        if (rsa == null) {
            rsa = new int[rows.length];
        } // if null then preferred or minimum size being asked for sizes not
          // cached
        if (csa == null) {
            csa = new int[cols.length];
        } // if null then preferred or minimum size being asked for sizes not
          // cached

        hgt = calculateRowOrColSize(-1, (ins.top + ins.bottom), rows, rsa, false, pref);
        wid = calculateRowOrColSize(-1, (ins.left + ins.right), cols, csa, true, pref);

//        if (diags) {
//            log.println((pref ? "Preferred" : "Minimum") + " layout size is [W=" + wid + ", H=" + hgt + "]");
//            log.println("Rows:");
//            for (int xa = 0; xa < rsa.length; xa++) {
//                log.println("  R" + (xa < 10 ? "0" : "") + xa + "=" + rsa[xa]);
//            }
//            log.println("Columns:");
//            for (int xa = 0; xa < csa.length; xa++) {
//                log.println("  C" + (xa < 10 ? "0" : "") + xa + "=" + csa[xa]);
//            }
//        }

        return new Dimension(wid, hgt);
    }

    private int calculateRowOrColSize(int consiz, int ins, RowCol[] rca, int[] sizes, boolean width, boolean pref) {
        int alc; // allocated
        int varcnt; // count of variable rows/columns

        alc = ins;
        varcnt = 0;

        // SET FIXED SIZES (ABSOLUTE AND PREFERRED), AND VALIDATE/CLEAR VARIABLE
        // ROWS/COLUMNS
        for (int xa = 0, pt = 0; xa < rca.length; xa++) {
            RowCol rc = rca[xa];
            if (rc.sizeType == '!') {
                sizes[xa] = rc.size;
            } else if (rc.sizeType == '*') {
                sizes[xa] = (width ? getPrefOrMinWidthOfCol(xa, pref) : getPrefOrMinHeightOfRow(xa, pref));
            } else {
                // we do not set sizes for variable size rows/cols here; using
                // the loop to do some setup and validation.
                pt += rc.size;
                if ((xa + 1) == rca.length && pt != 10000) {
                    throw new Escape("Total of percentages for variable rows or columns is not exactly 100%");
                }
                sizes[xa] = 0;
                varcnt++;
            }
            alc += sizes[xa];
        }

        if (consiz == -1) { // CALCULATING PREFERRED SIZE
            consiz = alc;
            if (varcnt > 0) {
                int inc;

                // SET VARIABLE ROWS/COLUMNS USING PREFERRED SIZES OF
                // NON-SPANNING CELLS
                inc = 0;
                for (int xa = 0/*, max = 0*/; xa < rca.length; xa++) {
                    RowCol rc = rca[xa];
                    if (rc.sizeType == '%') {
                        int ps = (width ? getPrefOrMinWidthOfCol(xa, pref) : getPrefOrMinHeightOfRow(xa, pref));
                        inc = Math.max(inc, calculateReversePercentage(ps, rc.size));
                    }
                }
                if (inc > 0) {
                    allocateVariableSpace(rca, sizes, inc, varcnt);
                    consiz += inc;
                }

                // SET VARIABLE ROWS/COLUMNS USING PREFERRED SIZES OF SPANNING
                // CELLS
                inc = 0;
                for (int xa = 0; xa < rows.length; xa++) {
                    for (int xb = 0; xb < cols.length; xb++) {
                        Component cm = components[xa][xb];
                        CellConstraints cs = constraints[xa][xb];
                        int tp = 0; // total percentage
                        int ts = 0; // total size of component
                        int pw;

                        if (width) {
                            if (isOccupied(cm, cs) && cs.hSpan > 1) {
                                pw = adjustForInsets(getPrefOrMinDimension(cm, cs, pref), cs, xa, xb).width;
                                for (int xc = xb, end = (xb + cs.hSpan); xc < rca.length && xc < end; xc++) {
                                    if (rca[xc].sizeType == '%') {
                                        tp += rca[xc].size;
                                    }
                                    ts += sizes[xc];
                                }
                                if (ts < pw) {
                                    inc = Math.max(inc, (calculateReversePercentage((pw - ts), tp))); // increase
                                                                                                      // container
                                                                                                      // width
                                                                                                      // by
                                                                                                      // an
                                                                                                      // amount
                                                                                                      // such
                                                                                                      // that
                                                                                                      // my
                                                                                                      // proportion
                                                                                                      // is
                                                                                                      // equal
                                                                                                      // to
                                                                                                      // (pw-ts)
                                }
                            }
                        } else {
                            if (isOccupied(cm, cs) && cs.vSpan > 1) {
                                int ph = adjustForInsets(getPrefOrMinDimension(cm, cs, pref), cs, xa, xb).height;
                                for (int xc = xa, end = (xa + cs.vSpan); xc < rows.length && xc < end; xc++) {
                                    if (rows[xc].sizeType == '%') {
                                        tp += rows[xc].size;
                                    }
                                    ts += sizes[xc];
                                }
                                if (ts < ph) {
                                    inc = Math.max(inc, (calculateReversePercentage((ph - ts), tp))); // increase
                                                                                                      // container
                                                                                                      // height
                                                                                                      // by
                                                                                                      // an
                                                                                                      // amount
                                                                                                      // such
                                                                                                      // that
                                                                                                      // my
                                                                                                      // proportion
                                                                                                      // is
                                                                                                      // equal
                                                                                                      // to
                                                                                                      // (pw-ts)
                                }
                            }
                        }
                    }
                }
                if (inc > 0) {
                    allocateVariableSpace(rca, sizes, inc, varcnt);
                    consiz += inc;
                }
            }
        } else if (alc < consiz) { // ALLOCATING SPACE FOR LAYOUT
            // ALLOCATE REMAINING SPACE TO VARIABLE COLUMNS
            allocateVariableSpace(rca, sizes, (consiz - alc), varcnt);
        }

        return consiz;
    }

    private void allocateVariableSpace(RowCol[] rca, int[] sizes, int varspc, int varcnt) {
        for (int xa = 0, rmn = varspc; xa < rca.length && varcnt > 0; xa++) {
            RowCol rc = rca[xa];
            if (rc.sizeType == '%') {
                int sz;
                if ((--varcnt) == 0) {
                    sz = rmn;
                } else {
                    sz = calculatePercentage(varspc, rc.size);
                }
                sizes[xa] += sz;
                rmn -= sz;
            }
        }
    }

    private Dimension adjustForInsets(Dimension siz, CellConstraints cns, int row, int col) {
        if (row != 0) {
            siz.height += cns.vInsets[0];
        }
        if (row != (rows.length - 1)) {
            siz.height += cns.vInsets[1];
        }
        if (col != 0) {
            siz.width += cns.hInsets[0];
        }
        if (col != (cols.length - 1)) {
            siz.width += cns.hInsets[1];
        }
        return siz;
    }

    private int calculatePercentage(int val, int ppt) {
        // If cell is 1000, and component is filling 50% of cell, the component
        // size is 1/2 the cell size:
        // ((1000*3333)/10000)=333 (integer truncated)
        if (ppt == 0) {
            return 0;
        } else if (ppt == 10000) {
            return val;
        } else if (ppt < 0 || ppt > 10000) {
            throw new Escape("Percentage value " + pptToString(ppt) + " not within the allowed range of 0.00-100.00");
        }
        return (int) ((val * (long) ppt) / 10000);
    }

    private int calculateReversePercentage(int val, int ppt) {
        // We want X in [X * ppt% = val * 100] :
        // therefore [X = (val * 100) / ppt%], therefore [X = (val*10000)/ppt]
        if (ppt == 0) {
            return 0;
        } else if (ppt == 10000) {
            return val;
        } else if (ppt < 0 || ppt > 10000) {
            throw new Escape("Percentage value " + pptToString(ppt) + " not within the allowed range of 0.00-100.00");
        }
        return (int) ((val * 10000L) / ppt);
    }

    private int getPrefOrMinHeightOfRow(int row, boolean pref) {
        int ms; // maximum size

        ms = 0;
        for (int xa = 0; xa < cols.length; xa++) {
            Component cm = components[row][xa];
            CellConstraints cs = constraints[row][xa];
            if (isOccupied(cm, cs) && cs.vSpan == 1) {
                ms = Math.max(ms, adjustForInsets(getPrefOrMinDimension(cm, cs, pref), cs, row, xa).height);
            }
        }
        return ms;
    }

    private int getPrefOrMinWidthOfCol(int col, boolean pref) {
        int ms; // maximum size

        ms = 0;
        for (int xa = 0; xa < rows.length; xa++) {
            Component cm = components[xa][col];
            CellConstraints cs = constraints[xa][col];
            if (isOccupied(cm, cs) && cs.hSpan == 1) {
                ms = Math.max(ms, adjustForInsets(getPrefOrMinDimension(cm, cs, pref), cs, xa, col).width);
            }
        }
        return ms;
    }

    private Dimension getPrefOrMinDimension(Component cm, CellConstraints cs, boolean pref) {
        Dimension ss; // size

        // GET BASE SIZE
        ss = _getPrefOrMinDimension(cm, cs, pref);

        // ADJUST FOR GROUPING
        if (!isBlank(cs.vGroup) || !isBlank(cs.hGroup)) {
            for (int rr = 0; rr < rows.length; rr++) {
                for (int cc = 0; cc < cols.length; cc++) {
                    Component gcm = components[rr][cc]; // component
                    CellConstraints gcs = constraints[rr][cc]; // constraints
                    if (isOccupied(gcm, gcs)) {
                        if (!isBlank(cs.vGroup) && gcs != null && gcs.vGroup != null && gcs.vGroup.equals(cs.vGroup)) {
                            ss.height = Math.max(ss.height, _getPrefOrMinDimension(gcm, gcs, pref).height);
                        }
                        if (!isBlank(cs.hGroup) && gcs != null && gcs.hGroup != null && gcs.hGroup.equals(cs.hGroup)) {
                            ss.width = Math.max(ss.width, _getPrefOrMinDimension(gcm, gcs, pref).width);
                        }
                    }
                }
            }
        }

        return ss;
    }

    private Dimension _getPrefOrMinDimension(Component cm, CellConstraints cs, boolean pref) {
        Dimension ss; // size

        ss = new Dimension(pref ? cm.getPreferredSize() : cm.getMinimumSize()); // create
                                                                                // copy
                                                                                // -
                                                                                // a
                                                                                // bug
                                                                                // in
                                                                                // 1.4.2.05
                                                                                // VM
                                                                                // occasionally
                                                                                // returns
                                                                                // the
                                                                                // original
        if (ss.height < 0) {
            ss.height = 1;
        }
        if (ss.width < 0) {
            ss.width = 1;
        }
        return ss;
    }

    // *************************************************************************************************
    // INSTANCE METHODS - GENERAL UTILITY
    // *************************************************************************************************

//    private boolean isSpanned(Component com, CellConstraints cns) {
//        return (com == null && cns != null);
//    }

    private boolean isOccupied(Component com, CellConstraints cns) {
        return (com != null && cns != null);
    }

    // *************************************************************************************************
    // INSTANCE INNER CLASSES
    // *************************************************************************************************

    // *************************************************************************************************
    // STATIC NESTED CLASSES - ROW/COL SPECIFICATION
    // *************************************************************************************************

    static private class RowCol extends Object {
        int size = 0;
        char sizeType = '*';
        int cellSpan = -1;
        int cellAlign = -1;
        int[] cellInsets = new int[] { -1, -1 };
        String cellGroup = null;

        RowCol(String txt) {
            StringTokenizer st; // string tokenizer

            if (txt == null) {
                return;
            }

            st = new StringTokenizer(txt, " \t", false);
            while (st.hasMoreTokens()) {
                String ct = st.nextToken();
                int ei = ct.indexOf('=');
                String cn, cv;

                if (ei == -1) {
                    throw new Escape("MatrixLayout Attribute \"" + ct + "\" is invalid - it must be Attribute=Value (Row/Column Constraints " + txt + ")");
                }
                cn = ct.substring(0, ei).trim().toLowerCase();
                cv = ct.substring(ei + 1).trim().toLowerCase();
                if (cn.length() == 0) {
                    throw new Escape("MatrixLayout Attribute \"" + ct + "\" is invalid - Attribute name is blank (Row/Column Constraints " + txt + ")");
                }
                if (cv.length() == 0) {
                    throw new Escape("MatrixLayout Attribute \"" + ct + "\" is invalid - Attribute value is blank (Row/Column Constraints " + txt + ")");
                }

                if (cn.equals("size")) {
                    if (cv.equals("preferred")) {
                        sizeType = '*';
                        cv = "0";
                    } else if (cv.equals("pref")) {
                        sizeType = '*';
                        cv = "0";
                    } else if (cv.endsWith("%")) {
                        sizeType = '%';
                        cv = cv.substring(0, cv.length() - 1);
                    } else if (cv.endsWith("!")) {
                        sizeType = '!';
                        cv = cv.substring(0, cv.length() - 1);
                    } else {
                        sizeType = '!';
                    }
                    if (sizeType == '%') {
                        size = parsePercPoints(cv, "Property: size=n.nn%");
                    } else {
                        size = Integer.parseInt(cv);
                    }
                } else if (cn.equals("cellspan")) {
                    if (cv.equals("*")) {
                        cv = "0";
                    }
                    cellSpan = Integer.parseInt(cv);
                } else if (cn.equals("cellalign")) {
                    if (cv.equals("left")) {
                        cellAlign = LEFT_TOP;
                    } else if (cv.equals("center")) {
                        cellAlign = MIDDLE;
                    } else if (cv.equals("right")) {
                        cellAlign = RIGHT_BOTTOM;
                    } else if (cv.equals("top")) {
                        cellAlign = LEFT_TOP;
                    } else if (cv.equals("middle")) {
                        cellAlign = MIDDLE;
                    } else if (cv.equals("bottom")) {
                        cellAlign = RIGHT_BOTTOM;
                    } else if (cv.equals("fill")) {
                        cellAlign = FILL;
                    } else {
                        throw new Escape("MatrixLayout Attribute \"" + ct + "\" is invalid - hAlign value '" + cv + "' is not permitted (Row/Column Constraints: " + txt + ")");
                    }
                } else if (cn.equals("cellinsets")) {
                    Insets ins = parseInsets(cv);
                    if (ins.top != -1) {
                        cellInsets[0] = ins.top;
                    }
                    if (ins.left != -1) {
                        cellInsets[1] = ins.left;
                    } // left because parseInsets will parse 2 values into top
                      // and left
                } else if (cn.equals("cellgroup")) {
                    cellGroup = cv.toLowerCase();
                } else if (cn.equals("cellgroup")) {
                    cellGroup = cv.toLowerCase();
                } else {
                    throw new Escape("MatrixLayout constraint attribute \"" + cn + "\" is not recognized (Row/Column Constraints: " + txt + ")");
                }
            }
        }

        public String toString() {
            return ("Row/Col Constraints [size=" + size + ", sizeType='" + sizeType + "', cellAlign=" + cellAlign + ", cellGroup=" + cellGroup + ", cellInsets[0]=" + cellInsets[0]
                    + ", cellInsets[1]=" + cellInsets[1] + "], cellSpan=" + cellSpan);
        }
    }

    // *************************************************************************************************
    // STATIC NESTED CLASSES - CELL CONSTRAINTS
    // *************************************************************************************************

    static private class CellConstraints extends Object {
        int row = -1;
        int vSpan = -1;
        int vAlign = -1;
        String vGroup = null;
        int[] vInsets = new int[] { -1, -1 };

        int col = -1;
        int hSpan = -1;
        int hAlign = -1;
        String hGroup = null;
        int[] hInsets = new int[] { -1, -1 };

        CellConstraints(String txt) {
            StringTokenizer st; // string tokenizer

            if (txt == null) {
                return;
            }

            st = new StringTokenizer(txt, " \t", false);
            while (st.hasMoreTokens()) {
                String ct = st.nextToken();
                int ei = ct.indexOf('=');
                String cn, cv;

                if (ei == -1) {
                    throw new Escape("MatrixLayout Attribute \"" + ct + "\" is invalid - it must be Attribute=Value (Cell Constraints: " + txt + ")");
                }
                cn = ct.substring(0, ei).trim().toLowerCase();
                cv = ct.substring(ei + 1).trim().toLowerCase();
                if (cn.length() == 0) {
                    throw new Escape("MatrixLayout Attribute \"" + ct + "\" is invalid - Attribute name is blank (Cell Constraints: " + txt + ")");
                }
                if (cv.length() == 0) {
                    throw new Escape("MatrixLayout Attribute \"" + ct + "\" is invalid - Attribute value is blank (Cell Constraints: " + txt + ")");
                }

                if (cn.equals("row")) {
                    if (cv.equals("current")) {
                        row = CURRENT;
                    } else if (cv.equals("cur")) {
                        row = CURRENT;
                    } else if (cv.equals("next")) {
                        row = NEXT;
                    } else {
                        row = Integer.parseInt(cv);
                        if (row < 1) {
                            throw invalidConstraint(ct, cv, txt);
                        }
                    }
                } else if (cn.equals("vspan")) {
                    if (cv.equals("*")) {
                        cv = "0";
                    }
                    if ((vSpan = Integer.parseInt(cv)) < 0) {
                        throw invalidConstraint(ct, cv, txt);
                    }
                } else if (cn.equals("valign")) {
                    if (cv.equals("top")) {
                        vAlign = LEFT_TOP;
                    } else if (cv.equals("center")) {
                        vAlign = MIDDLE;
                    } else if (cv.equals("middle")) {
                        vAlign = MIDDLE;
                    } else if (cv.equals("bottom")) {
                        vAlign = RIGHT_BOTTOM;
                    } else if (cv.equals("fill")) {
                        vAlign = FILL;
                    } else {
                        throw new Escape("MatrixLayout Attribute \"" + ct + "\" is invalid - vAlign value '" + cv + "' is not permitted (Cell Constraints: " + txt + ")");
                    }
                } else if (cn.equals("vgroup")) {
                    vGroup = cv.toLowerCase();
                } else if (cn.equals("col")) {
                    if (cv.equals("current")) {
                        col = CURRENT;
                    } else if (cv.equals("cur")) {
                        col = CURRENT;
                    } else if (cv.equals("next")) {
                        col = NEXT;
                    } else {
                        col = Integer.parseInt(cv);
                        if (col < 1) {
                            throw invalidConstraint(ct, cv, txt);
                        }
                    }
                } else if (cn.equals("hspan")) {
                    if (cv.equals("*")) {
                        cv = "0";
                    }
                    if ((hSpan = Integer.parseInt(cv)) < 0) {
                        throw invalidConstraint(ct, cv, txt);
                    }
                } else if (cn.equals("halign")) {
                    if (cv.equals("left")) {
                        hAlign = LEFT_TOP;
                    } else if (cv.equals("center")) {
                        hAlign = MIDDLE;
                    } else if (cv.equals("middle")) {
                        hAlign = MIDDLE;
                    } else if (cv.equals("right")) {
                        hAlign = RIGHT_BOTTOM;
                    } else if (cv.equals("fill")) {
                        hAlign = FILL;
                    } else {
                        throw new Escape("MatrixLayout Attribute \"" + ct + "\" is invalid - hAlign value '" + cv + "' is not permitted (Cell Constraints: " + txt + ")");
                    }
                } else if (cn.equals("hgroup")) {
                    hGroup = cv.toLowerCase();
                } else if (cn.equals("insets")) {
                    Insets ins = parseInsets(cv);
                    if (ins.top != -1) {
                        vInsets[0] = ins.top;
                    }
                    if (ins.left != -1) {
                        hInsets[0] = ins.left;
                    }
                    if (ins.bottom != -1) {
                        vInsets[1] = ins.bottom;
                    }
                    if (ins.right != -1) {
                        hInsets[1] = ins.right;
                    }
                } else {
                    throw new Escape("MatrixLayout Constraints \"" + ct + "\" is not recognized (Cell Constraints: " + txt + ")");
                }
            }
        }

        private Escape invalidConstraint(String ct, String cv, String txt) {
            return invalidConstraint(ct, cv, txt, "is not permitted");
        }

        private Escape invalidConstraint(String ct, String cv, String txt, String msg) {
            return new Escape("MatrixLayout Attribute \"" + ct + "\" is invalid - vSpan value '" + cv + "' " + msg + " (Constraints Text: " + txt + ")");
        }

        void resolveConstraints(MatrixLayout lyo) {
            CellConstraints cd = lyo.defaults; // container defaults
            RowCol[] ra = lyo.rows; // row array
            RowCol[] ca = lyo.cols; // colum array

            // FIRST, ROW/COLUMN ARE NEEDED FOR ROW/COLUMN DEFAULTS
            if (row == -1) {
                row = cd.row;
            }
            if (col == -1) {
                col = cd.col;
            }
            if (row == NEXT) {
                row = (lyo.lastConstraints == null ? 1 : (lyo.lastConstraints.row + 1 + lyo.lastConstraints.vSpan));
            } else if (row == CURRENT) {
                row = (lyo.lastConstraints == null ? 1 : (lyo.lastConstraints.row + 1));
            }
            if (col == NEXT) {
                col = (lyo.lastConstraints == null ? 1 : (lyo.lastConstraints.col + 1 + lyo.lastConstraints.hSpan));
            } else if (col == CURRENT) {
                col = (lyo.lastConstraints == null ? 1 : (lyo.lastConstraints.col + 1));
            }
            if (row > ra.length) {
                throw new Escape("Number of rows exceeded in MatrixLayout (Specified: " + row + " rows, Limit: " + ra.length + ").");
            }
            if (col > ca.length) {
                throw new Escape("Number of columns exceeded in MatrixLayout (Specified: " + col + " cols, Limit: " + ca.length + ").");
            }
            row--;
            col--;

            // SECOND, APPLY ROW/COLUMN DEFAULTS
            if (hAlign == -1) {
                hAlign = ca[col].cellAlign;
            }
            if (hGroup == null) {
                hGroup = ca[col].cellGroup;
            }
            if (hInsets[0] == -1) {
                hInsets[0] = ca[col].cellInsets[0];
            }
            if (hInsets[1] == -1) {
                hInsets[1] = ca[col].cellInsets[1];
            }
            if (hSpan == -1) {
                hSpan = ca[col].cellSpan;
            }
            if (vAlign == -1) {
                vAlign = ra[row].cellAlign;
            }
            if (vGroup == null) {
                vGroup = ra[row].cellGroup;
            }
            if (vInsets[0] == -1) {
                vInsets[0] = ra[row].cellInsets[0];
            }
            if (vInsets[1] == -1) {
                vInsets[1] = ra[row].cellInsets[1];
            }
            if (vSpan == -1) {
                vSpan = ra[row].cellSpan;
            }

            // THIRD APPLY CONTAINER DEFAULTS
            if (hAlign == -1) {
                hAlign = cd.hAlign;
            }
            if (hGroup == null) {
                hGroup = cd.hGroup;
            }
            if (hInsets[0] == -1) {
                hInsets[0] = cd.hInsets[0];
            }
            if (hInsets[1] == -1) {
                hInsets[1] = cd.hInsets[1];
            }
            if (hSpan == -1) {
                hSpan = cd.hSpan;
            }
            if (vAlign == -1) {
                vAlign = cd.vAlign;
            }
            if (vGroup == null) {
                vGroup = cd.vGroup;
            }
            if (vInsets[0] == -1) {
                vInsets[0] = cd.vInsets[0];
            }
            if (vInsets[1] == -1) {
                vInsets[1] = cd.vInsets[1];
            }
            if (vSpan == -1) {
                vSpan = cd.vSpan;
            }

            // LAST APPLY CLASS DEFAULTS
            resolveDefaults();

            if (vSpan == 0) {
                vSpan = (ra.length - row);
            }
            if (hSpan == 0) {
                hSpan = (ca.length - col);
            }

            if ((row + vSpan) > ra.length) {
                throw new Escape("Number of rows exceeded in MatrixLayout (Specified: " + (row + vSpan) + " rows, Limit: " + ra.length + ").");
            }
            if ((col + hSpan) > ca.length) {
                throw new Escape("Number of columns exceeded in MatrixLayout (Specified: " + (col + hSpan) + " cols, Limit: " + ca.length + ").");
            }
        }

        void resolveDefaults() {
            if (col == -1) {
                col = NEXT;
            }
            if (hAlign == -1) {
                hAlign = LEFT_TOP;
            }
            if (hGroup == null) {
                hGroup = null;
            }
            if (hInsets[0] == -1) {
                hInsets[0] = 3;
            }
            if (hInsets[1] == -1) {
                hInsets[1] = 3;
            }
            if (hSpan == -1) {
                hSpan = 1;
            }
            if (row == -1) {
                row = CURRENT;
            }
            if (vAlign == -1) {
                vAlign = MIDDLE;
            }
            if (vGroup == null) {
                vGroup = null;
            }
            if (vInsets[0] == -1) {
                vInsets[0] = 3;
            }
            if (vInsets[1] == -1) {
                vInsets[1] = 3;
            }
            if (vSpan == -1) {
                vSpan = 1;
            }
        }

        public String toString() {
            String rw = (row == NEXT ? "Next" : (row == CURRENT ? "Current" : String.valueOf(row + 1)));
            String cl = (col == NEXT ? "Next" : (col == CURRENT ? "Current" : String.valueOf(col + 1)));
            return ("CellContraints [row=" + rw + ", col=" + cl + ", hAlign=" + hAlign + ", hGroup=" + hGroup + ", hInsets[0]=" + hInsets[0] + ", hInsets[1]=" + hInsets[1] + "], hSpan=" + hSpan
                    + ", vAlign=" + vAlign + ", vGroup=" + vGroup + ", vInsets[0]=" + vInsets[0] + ", vInsets[1]=" + vInsets[1] + ", vSpan=" + vSpan);
        }
    }

    // *************************************************************************************************
    // STATIC NESTED CLASSES - ESCAPE (COULD DROP THIS FOR RUNTIME EXCEPTION
    // *************************************************************************************************

    @SuppressWarnings("serial")
    static public class Escape extends RuntimeException {
        Escape(String txt) {
            super(txt);
        }
    }

    // *************************************************************************************************
    // STATIC PROPERTIES
    // *************************************************************************************************

    static public final Component NULL = new Container(); // convenience for
                                                          // adding null
                                                          // components

//    static private boolean diags = false; // whether to output diagnostic
//                                          // messages
//
//    static private final int PREFERRED = 1; // calculate sizes for preferred
//                                            // size
//    static private final int MINIMUM = 2; // calculate sizes for minimum size
//    static private final int LAYOUT = 3; // calculate sizes for layout

    static private final int LEFT_TOP = 1;
    static private final int MIDDLE = 2;
    static private final int RIGHT_BOTTOM = 3;
    static private final int FILL = 4;

    static private final int CURRENT = (Integer.MAX_VALUE);
    static private final int NEXT = (Integer.MAX_VALUE - 1);

//    static private final dol.lpc.lib.env.ConsoleWriter log = new dol.lpc.lib.env.ConsoleWriter("Matrix Layout Manager"); // log
                                                                                                                         // for
                                                                                                                         // diagnostics

    // *************************************************************************************************
    // STATIC INIT & MAIN
    // *************************************************************************************************

    static {
        NULL.setLocation(-1, -1);
        NULL.setSize(0, 0);
    }

    // *************************************************************************************************
    // STATIC METHODS - PRIVATE SUPPORTING
    // *************************************************************************************************

//    static private final java.lang.reflect.Method IS_MAXIMUM_SIZE_SET;
//    static {
//        java.lang.reflect.Method mth = null;
//        try {
//            mth = Component.class.getMethod("isMaximumSizeSet", (Class[]) null);
//        } catch (Throwable thr) {
//            mth = null;
//        }
//        IS_MAXIMUM_SIZE_SET = mth;
//    }

//    static private boolean isMaximumSizeSet(Component com) {
//        if (IS_MAXIMUM_SIZE_SET != null) {
//            try {
//                return ((Boolean) IS_MAXIMUM_SIZE_SET.invoke(com, (Object[]) null)).booleanValue();
//            } catch (Throwable thr) {
//                ;
//            }
//        }
//        return false;
//    }

    // *************************************************************************************************
    // STATIC METHODS - PUBLIC UTILITIY
    // *************************************************************************************************

    /**
     * Enable diagnostic messages for all MatrixLayouts.
     */
//    static public void setDiagnostics(boolean val) {
//        diags = val;
//    }

    /**
     * Create a fixed size panel with a 1 row layout using the components
     * supplied. Useful for a series of related components like a field with a
     * hint next to it.
     * 
     * @param com
     *            The components to put into the new panel.
     */
    /* +J5 */static public JPanel singleRowBar(Component... com) {
        // -J5*/static public JPanel singleRowBar(Component[] com) {
        return singleRowBar(5, -1, false, com);
    }

    /**
     * Create an expanding panel with a 1 row layout using the components
     * supplied. Useful for a series of related components when you cannot
     * assign them into columns in the overall layout.
     * 
     * @param spc
     *            Cell spacing.
     * @param exp
     *            The column number (1+) of the (single) column that is to
     *            expand horizontally.
     * @param com
     *            The components to put into the new panel.
     */
    /* +J5 */static public JPanel singleRowBar(int spc, int exp, Component... com) {
        // -J5*/static public JPanel singleRowBar(int spc, int exp, Component[]
        // com) {
        return singleRowBar(spc, exp, false, com);
    }

    /**
     * Create a fixed size panel with a 1 row layout using the components
     * supplied. Useful for a series of related components like a button bar
     * when all components should be their preferred size.
     * 
     * @param spc
     *            Cell spacing.
     * @param equ
     *            If true all cells will be grouped to get equal size.
     * @param com
     *            The components to put into the new panel.
     */
    /* +J5 */static public JPanel singleRowBar(int spc, boolean equ, Component... com) {
        // -J5*/static public JPanel singleRowBar(int spc, boolean equ,
        // Component[] com) {
        return singleRowBar(spc, -1, equ, com);
    }

    static private JPanel singleRowBar(int spc, int exp, boolean equ, Component[] com) {
        JPanel ep; // JPanel
        String[] ra; // row array
        String[] ca; // column array

        if (equ) {
            ra = new String[] { "size=Preferred cellAlign=Fill   cellInsets=0 cellGroup=All" };
        } else {
            ra = new String[] { "size=Preferred cellAlign=Middle cellInsets=0" };
        }
        ca = new String[com.length];
        for (int xa = 0; xa < com.length; xa++) {
            ca[xa] = "";
            if (equ) {
                ca[xa] += ("Size=Pref cellAlign=Fill cellInsets=0," + spc + " cellGroup=All");
            } else if ((xa + 1) == exp) {
                ca[xa] += ("Size=100% cellAlign=Fill cellInsets=0," + spc);
            } else {
                ca[xa] += ("Size=Pref cellAlign=Left cellInsets=0," + spc);
            }
        }
        ep = new JPanel(new MatrixLayout(ra, ca), false);
        for (int xa = 0; xa < com.length; xa++) {
            ep.add(com[xa]);
        }
        return ep;
    }

    /* +J5 */static public JPanel singleRowBar(String colspc, Component... com) {
        // -J5*/static public JPanel singleRowBar(String colspc, Component[]
        // com) {
        return singleRowBar(colspc, "hAlign=Fill", com);
    }

    /* +J5 */static public JPanel singleRowBar(String colspc, String dfts, Component... com) {
        // -J5*/static public JPanel singleRowBar(String colspc, String dfts,
        // Component[] com) {
        JPanel ep; // JPanel

        ep = new JPanel(new MatrixLayout("Pref", colspc, dfts));
        for (int xa = 0; xa < com.length; xa++) {
            ep.add(com[xa], "Row=1 Col=Next");
        }
        return ep;
    }

    /**
     * Create an expanding panel with a 1 column layout using the components
     * supplied. Useful for a series of related components when you cannot
     * assign them into rows in the overall layout.
     * 
     * @param spc
     *            Cell spacing.
     * @param exp
     *            The row number (1+) of the (single) row that is to expand
     *            vertically.
     * @param com
     *            The components to put into the new panel.
     */
    /* +J5 */static public JPanel singleColStack(int spc, int exp, Component... com) {
        // -J5*/static public JPanel singleColStack(int spc, int exp,
        // Component[] com) {
        return singleColStack(spc, exp, false, com);
    }

    /**
     * Create a fixed size panel with a 1 row layout using the components
     * supplied. Useful for a series of related components like a button stack
     * when all components should be their preferred size.
     * 
     * @param spc
     *            Cell spacing.
     * @param equ
     *            If true all cells will be grouped to get equal size.
     * @param com
     *            The components to put into the new panel.
     */
    /* +J5 */static public JPanel singleColStack(int spc, boolean equ, Component... com) {
        // -J5*/static public JPanel singleColStack(int spc, boolean equ,
        // Component[] com) {
        return singleColStack(spc, -1, equ, com);
    }

    static private JPanel singleColStack(int spc, int exp, boolean equ, Component[] com) {
        JPanel ep; // JPanel
        String[] ra; // row array
        String[] ca; // column array

        if (equ) {
            ca = new String[] { "size=Preferred cellAlign=Fill   cellInsets=0 cellGroup=All" };
        } else {
            ca = new String[] { "size=Preferred cellAlign=Middle cellInsets=0" };
        }
        ra = new String[com.length];
        for (int xa = 0; xa < com.length; xa++) {
            ra[xa] = "";
            if (equ) {
                ra[xa] += ("Size=Pref cellAlign=Fill cellInsets=0," + spc + " cellGroup=All");
            } else if ((xa + 1) == exp) {
                ra[xa] += ("Size=100% cellAlign=Fill cellInsets=0," + spc);
            } else {
                ra[xa] += ("Size=Pref cellAlign=Top  cellInsets=0," + spc);
            }
        }
        ep = new JPanel(new MatrixLayout(ca, ra), false);
        for (int xa = 0; xa < com.length; xa++) {
            ep.add(com[xa]);
        }
        return ep;
    }

    /* +J5 */static public JPanel singleColStack(String rowspc, Component... com) {
        // -J5*/static public JPanel singleColStack(String rowspc, Component[]
        // com) {
        return singleColStack(rowspc, "vAlign=Fill", com);
    }

    /* +J5 */static public JPanel singleColStack(String rowspc, String dfts, Component... com) {
        // -J5*/static public JPanel singleColStack(String rowspc, String dfts,
        // Component[] com) {
        JPanel ep; // JPanel

        ep = new JPanel(new MatrixLayout(rowspc, "Pref", dfts));
        for (int xa = 0; xa < com.length; xa++) {
            ep.add(com[xa], "Row=Next Col=1");
        }
        return ep;
    }

    /**
     * Create a layout for 1 component expanding to consume the entire
     * container.
     */
    static public MatrixLayout fillContainer() {
        return new MatrixLayout("100%", "100%", "Row=1 Col=1 vAlign=Fill hAlign=Fill insets=0,0:0,0");
    }

    /**
     * Creates an array for standard row or column specifications. A convenience
     * method to allow easy creation of extended row/column arrays where the
     * specifications for many/most are the same.
     */
    static public String[] arrayOf(int siz, String val) {
        String[] ra; // return array

        ra = new String[siz];
        for (int xa = 0; xa < siz; xa++) {
            ra[xa] = val;
        }
        return ra;
    }

    static private boolean isBlank(String val) {
        return (val == null || val.trim().length() == 0);
    }

    static private int parsePercPoints(String str, String errdtl) {
        String[] gpsa; // general purpose string array
        int iv1, iv2; // integer work value 1 and 2

        if (str.endsWith("%")) {
            str = str.substring(0, str.length() - 1);
        }
        gpsa = parseList(str, ".");
        if (gpsa.length > 2 || (gpsa.length == 2 && gpsa[1].length() > 2)) {
            throw new IllegalArgumentException("A percentage value must be of format n%, n.n% or n.nn% (" + errdtl + ")");
        }
        if (gpsa.length == 2 && gpsa[1].length() == 0) {
            gpsa[1] = "00";
        }
        if (gpsa.length == 2 && gpsa[1].length() == 1) {
            gpsa[1] += "0";
        }
        if ((iv1 = parseInt(gpsa[0], Integer.MAX_VALUE)) == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("The whole part of the percentage value was not a valid number (" + errdtl + ")");
        }
        if (iv1 < 0 || iv1 > 100) {
            throw new IllegalArgumentException("The whole part of the percentage value was not within the range [0.00 >= x >= 100.00] (" + errdtl + ")");
        }
        iv2 = 0;
        if (gpsa.length > 1 && (iv2 = parseInt(gpsa[1], Integer.MAX_VALUE)) == Integer.MAX_VALUE) {
            throw new IllegalArgumentException("The decimal part of the percentage value was not a valid number (" + errdtl + ")");
        }
        if (iv2 < 0 || iv2 > 99 || (iv2 != 0 && iv1 == 100)) {
            throw new IllegalArgumentException("The decimal part of the percentage value was not within the range [0 >= x >= 99] (" + errdtl + ")");
        }
        if (iv1 != 0 || iv2 != 0) {
            return ((iv1 * 100) + iv2);
        } else {
            return 0;
        }
    }

    static private String[] parseList(String str, String sep) {
        StringTokenizer vt; // value tokenizer
        ArrayList<String> al; // array list for values

        vt = new StringTokenizer(str, sep, false);
        al = new ArrayList<String>(vt.countTokens());
        while (vt.hasMoreElements()) {
            al.add(vt.nextToken().trim());
        }
        return al.toArray(new String[al.size()]);
    }

    static private int parseInt(String str, int dft) {
        if (str == null) {
            return dft;
        }
        str = str.trim();
        if (str.length() < 1) {
            return dft;
        }
        if (str.charAt(0) == '+') {
            if (str.length() < 2) {
                return dft;
            }
            str = str.substring(1);
        }
        try {
            return Integer.parseInt(str, 10);
        } catch (NumberFormatException thr) {
            return dft;
        }
    }

    static private String pptToString(int ppt) {
        int wn = (ppt / 100);
        int dn = (ppt % 100);

        if (dn < 10) {
            return (wn + ".0" + dn + "%");
        } else {
            return (wn + "." + dn + "%");
        }
    }

    static public Insets parseInsets(String val) {
        if (val == null) {
            return null;
        }

        Insets ins = new Insets(-1, -1, -1, -1);
        int ioc = val.indexOf(':');
        String[] tlbr;

        if (ioc != -1) {
            tlbr = split(val, ':', new String[2]);
            String[] tl = split(tlbr[0], ',', new String[2]);
            String[] br = split(tlbr[1], ',', new String[2]);
            tlbr = new String[] { tl[0], tl[1], br[0], br[1] };
        } else {
            tlbr = split(val, ',', new String[4]);
        }

        try {
            ins.top = inset(tlbr[0], ins.top);
            ins.left = inset(tlbr[1], ins.left);
            ins.bottom = inset(tlbr[2], ins.bottom);
            ins.right = inset(tlbr[3], ins.right);
        } catch (Throwable thr) {
            throw new Escape("Insets value \"" + val + "\" is invalid: " + thr);
        }

        return ins;
    }

    static private String[] split(String txt, char chr, String[] tgt) {
        for (int xa = 0; xa < tgt.length; xa++) {
            int ioc = txt.indexOf(chr);
            if (ioc != -1) {
                tgt[xa] = txt.substring(0, ioc);
                txt = txt.substring(ioc + 1);
            } else {
                tgt[xa] = txt;
                txt = "";
            }
        }
        return tgt;
    }

    static private int inset(String val, int dft) {
        return ((val == null || val.equals("") || val.equals("dft") || val.equals("default")) ? dft : Integer.parseInt(val.trim()));
    }

} // END PUBLIC CLASS
