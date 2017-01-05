import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class MiniCPrintListener extends MiniCBaseListener {
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
    HashMap<String, Hash> funMap = new HashMap<String, Hash>(); //함수안의 변수들을 HashMap으로 저장해서 사용하기 위해 만듬
    HashMap<String, Hash> globalMap = new HashMap<String, Hash>();//전역변수에 대해 HashMap으로 저장해서 사용하기 위해 만듬
    int base = 0, offset = 0, funOffset = 0, labelNum = 0, ifNum = 0, ret = 0, retNum = 0;
    int result = 0;
    //이진연산자
    boolean isBinaryOperation(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 3 && ctx.getChild(1) != ctx.expr() && ctx.getChild(0) != ctx.IDENT();
    }
    //전위연산자
    boolean isPrefixOperation(MiniCParser.ExprContext ctx){
        return ctx.getChildCount() == 2 && ctx.getChild(0) != ctx.expr();
    }
    //11칸 space를 위해 만든 함수
    public String getSpace(){
        String space = "";

        space += "           ";

        return space;
    }
    //함수의 이름과 while, if문에 대한 라벨과 space를 사용하기 위한 함수
    public String getSpace(String fun){
        String space = "";
        if(fun.length() == 1)
            space = fun + "          ";
        else if(fun.length() == 2)
            space = fun + "         ";
        else if(fun.length() == 3)
            space = fun + "        ";
        else if(fun.length() == 4)
            space = fun + "       ";
        else if(fun.length() == 5)
            space = fun + "      ";
        else if(fun.length() == 6)
            space = fun + "     ";
        else if(fun.length() == 7)
            space = fun + "    ";
        else if(fun.length() == 8)
            space = fun + "   ";
        else if(fun.length() == 9)
            space = fun + "  ";
        else if(fun.length() == 10)
            space = fun + " ";
        return space;
    }
    //숫자인지 문자인지 구별해주기 위해 만든 함수
    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override public void enterProgram(MiniCParser.ProgramContext ctx) {

    }
    //모든 프로그램이 끝나고 난후에 마지막에 출력해주기 위한 함수
    @Override public void exitProgram(MiniCParser.ProgramContext ctx) {
        String text = "";
        for(int i = 0; i < ctx.decl().size(); i++){
            System.out.println(newTexts.get(ctx.decl(i)));
            text += newTexts.get(ctx.decl(i))+"\n";
        }
        //bgn은 전역변수의 size를 출력해기위해 사용한다. 그리고 ldp와 call main , end를 출력시켜준다.
        text += getSpace()+ "bgn " + offset + "\n" + getSpace() + "ldp\n" +
                getSpace() + "call main\n" + getSpace() + "end\n";
        System.out.println(getSpace()+ "bgn " + offset + "\n" + getSpace() + "ldp\n" +
                getSpace() + "call main\n" + getSpace() + "end\n");
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter("text.uco"));
            bw.write(text);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public void enterDecl(MiniCParser.DeclContext ctx) {

    }

    //function인지 변수인지 구별
    @Override public void exitDecl(MiniCParser.DeclContext ctx) {
        if(ctx.getChild(0) == ctx.var_decl()){
            newTexts.put(ctx, newTexts.get(ctx.var_decl()));
        }
        else if(ctx.getChild(0) == ctx.fun_decl()){
            newTexts.put(ctx, newTexts.get(ctx.fun_decl()));
        }
    }

    @Override public void enterVar_decl(MiniCParser.Var_declContext ctx) {

    }
    //전역변수에 대해 U-코드를 출력해주는 함수
    @Override public void exitVar_decl(MiniCParser.Var_declContext ctx) {
        String ldent = "", literal = "";
        //ex) int x; type_spec IDENT ';'
        int value = 0;
        if(ctx.getChildCount() == 3) {
            ldent = ctx.IDENT().getText();
            offset++;//offset은 전역변수에 대한 offset이기에 1을 증가시킨다.
            base = 1;//전역변수의 base는 전부 1이다.
            newTexts.put(ctx, getSpace() + "sym" + " " + base + " " + offset + " " + 1);
            globalMap.put(ldent,new Hash(base,offset,1));//globalMap haspMap에 키값과 그 키값에 해당하는 Hash를 저장
        }
        //ex) int x = 1; type_spec IDENT '=' LITERAL ';'
        else if(ctx.getChildCount() == 5) {
            ldent = ctx.IDENT().getText();
            literal = ctx.LITERAL().getText();
            value = Integer.parseInt(literal);
            offset++;//offset은 전역변수에 대한 offset이기에 1을 증가시킨다.
            base = 1;//전역변수의 base는 전부 1이다.
            //변수 선언과 함께 그 변수에 값을 출력해주는 과정이다.
            newTexts.put(ctx, getSpace() + "sym " + base + " " + offset + " " + 1 +
                    "\n"+ getSpace() + "ldc " + value+
                    "\n"+ getSpace() + "str " + base + " " + offset);
            //추가
            //변수에 값을 저장하기 때문에 그 값을 저장하고 나중에 사용하기 위해 사용
            globalMap.put(ldent,new Hash(base,offset,1,value, true));//변수의 값을 저장하고 값의 유무를 true로 한다.
        }
        //ex) int x[3]; type_spec IDENT '[' LITERAL ']' ';'
        else if(ctx.getChildCount() == 6){
            ldent = ctx.IDENT().getText();
            literal = ctx.LITERAL().getText();
            value = Integer.parseInt(literal);
            offset++;//offset은 전역변수에 대한 offset이기에 1을 증가시킨다.
            base = 1;//전역변수의 base는 전부 1이다.
            //배열에 대한 변수선언 배열의 size는 LITERAL을 사용해서 저장한다.
            newTexts.put(ctx, getSpace() + "sym " + base + " " + offset + " " + value);
            //globalMap haspMap에 키값과 그 키값에 해당하는 Hash를 저장
            globalMap.put(ldent,new Hash(base,offset,value));
            //배열 후의 offset이기에 전의 offset과 size를 더하고 1을 뺴주면 다음 offset이 나온다.
            offset = offset + value - 1;
        }
    }

    @Override public void enterType_spec(MiniCParser.Type_specContext ctx) {
    }

    @Override public void exitType_spec(MiniCParser.Type_specContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override public void enterFun_decl(MiniCParser.Fun_declContext ctx) {

    }

    //function에 대한 함수 type IDENT (params) {compound_stmt}
    @Override public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
        String ident, params, stmt, fun;
        ident = ctx.IDENT().getText();// IDENT
        params = newTexts.get(ctx.params());// params
        stmt = newTexts.get(ctx.compound_stmt());// compound_stmt
        base = 2;
        //retNum => 프로그램중 return이 하나라도 있는지 확인하기 위한 변수 , ret => 함수에 return이 있는지 확인하기 위한 변수
        //funoffset => 함수안의 변수들의 총 offset base => 블록번호 2 =>렉시컬레벨
        if(retNum == 1){//함수들 중에 하나라도 return 이 있는 경우
            if(ret == 1){//함수에 retrun이 있는 경우
                newTexts.put(ctx, getSpace(ident) + "proc " + funOffset + " " + base + " "+ 2 + "\n" +
                        params + stmt + getSpace() + "end");
            } else{//함수에 retrun 이 없는 경우
                newTexts.put(ctx, getSpace(ident) + "proc " + funOffset + " " + base + " "+ 2 + "\n" +
                        params + stmt + getSpace() + "ret\n" + getSpace() + "end");
            }
        } else {//함수들 중에 return이 하나도 없는 경우
            newTexts.put(ctx, getSpace(ident) + "proc " + funOffset + " " + base + " " + 2 + "\n" +
                    params + stmt + getSpace() + "ret\n"+ getSpace() + "end");
        }
        funMap.clear();
        funOffset = 0;
        ret = 0;
    }

    @Override public void enterParams(MiniCParser.ParamsContext ctx) {

    }

    //파라미터 들에 대한 함수
    @Override public void exitParams(MiniCParser.ParamsContext ctx) {
        String param = "";
        //파라미터가 없는 경우
        if(ctx.getChildCount() == 0){
            newTexts.put(ctx, param);
        }
        //파라미터가 하나 또는 여러개 인경우
        else if(ctx.getChildCount() >= 1){
            param = newTexts.get(ctx.param(0));
            for(int i = 1; i < ctx.param().size(); i++){
                param += newTexts.get(ctx.param(i));
            }
            newTexts.put(ctx, param);
        }
    }

    @Override public void enterParam(MiniCParser.ParamContext ctx) {

    }
    //끝
    //하나의 파라미터에 대한 함수 ex) int x // int x[] 배열이나 그냥 변수나 size는 1이기에 상관이 없다.
    @Override public void exitParam(MiniCParser.ParamContext ctx) {
        String ldent = "";
        ldent = ctx.IDENT().getText();
        funOffset++; // 함수의 파라미터에 대한 값이기에 funOffset을 1증가시킨다.
        base = 2;//base는 2이다.
        //파라미터의 모든 size는 1이다.
        newTexts.put(ctx, getSpace() +"sym " + base + " " + funOffset + " " + 1 + "\n");
        //funMap의 HashMap에 파라미터의 키값과 값들을 저장시킨다.
        funMap.put(ldent,new Hash(base,funOffset,1));
    }

    @Override public void enterStmt(MiniCParser.StmtContext ctx) {

    }
    //끝
    //객체 expr_stmt, compound_stmt, if_stmt, while_stmt, return_stmt
    @Override public void exitStmt(MiniCParser.StmtContext ctx) {
        newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
    }

    @Override public void enterExpr_stmt(MiniCParser.Expr_stmtContext ctx) {}
    //끝
    // expr ';'
    @Override public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
        newTexts.put(ctx, newTexts.get(ctx.expr()));
    }

    @Override public void enterWhile_stmt(MiniCParser.While_stmtContext ctx) {

    }
    //끝
    //while문에 대한 함수 ex) while (expr) stmt
    @Override public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
        String expr = "", stmt = "";

        String format = "$$" + Integer.toString(++labelNum);;//while을 시작하는 라벨이기에 1을 증가 시켜준다.
        String format2 = "$$" + Integer.toString(++labelNum);//while이 끝나는 부분이기에 라벨을 1더 증가 시켜준다.
        expr = newTexts.get(ctx.expr());//while의 조건문에 대한 변수
        stmt = newTexts.get(ctx.stmt());//while안에 코드에 대해 저장하기 위한 변수
        //while에 대한 u-코드
        newTexts.put(ctx, getSpace(format) + "nop\n" + expr +
                getSpace() +"fjp " + format2 + "\n" + stmt +
                getSpace() + "ujp " + format + "\n" +
                getSpace(format2) + "nop\n");
    }

    @Override public void enterCompound_stmt(MiniCParser.Compound_stmtContext ctx) {

    }
    //끝
    //'{' local_decl* stmt* '}'
    @Override public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
        String st1 = "", st2 = "";
        //local_decl
        for(int i = 0; i < ctx.local_decl().size(); i++){
            st1 += newTexts.get(ctx.local_decl(i));
        }
        //stmt
        for(int j = 0; j < ctx.stmt().size(); j++){
            st2 += newTexts.get(ctx.stmt(j));
        }
        newTexts.put(ctx, st1 + st2);
    }

    @Override public void enterLocal_decl(MiniCParser.Local_declContext ctx) {

    }
    //함수안에 있는 지역변수에 대해 U-코드를 만들어주는 함수
    @Override public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
        String ldent = "", literal = "";
        base = 2;//함수 안의 base는 전부 2
        int value;//배열의 size를 저장하기위해 만든 변수
        funOffset++;//함수안의 offset은 변수가 하나 있을때마다 증가하기에 1을 증가시킨다.
        //type_spec IDENT ';'
        if(ctx.getChildCount() == 3){
            ldent = ctx.IDENT().getText();//변수의 이름
            //U-코드 base는 2를 가지고 있고 funOffset은 1을 증가시켰다. 그리고 size는 1이다.
            newTexts.put(ctx, getSpace() + "sym " + base + " " + funOffset + " " + 1+"\n");
            //그 변수의 base, offset, size를 funMap이라는 Hash에 변수이름을 키값으로 갖게 저장시킨다.
            funMap.put(ldent,new Hash(base,funOffset,1));
        }
        //type_spec IDENT '=' LITERAL ';'
        else if(ctx.getChildCount() == 5){
            ldent = ctx.IDENT().getText();//변수의 이름
            literal = ctx.LITERAL().getText();//변수의 값
            value = Integer.parseInt(literal);//변수의 값
            //변수와 변수의 값을 저장하는 경우에는 value에 변수의 값을 저장시킨다.
            //U-코드에서 변수를 지정하고 변수의 값을 지정한후 다시 변수에 str해준다.
            newTexts.put(ctx, getSpace() + "sym " + base + " " + funOffset + " " + 1 +
                    "\n"+getSpace() + "ldc " + value+
                    "\n"+getSpace() + "str " + base + " " + funOffset+"\n");
            //그 변수의 이름을 키값으로 funMap에 base, offset, size를 저장시킨다.
            //추가
            funMap.put(ldent,new Hash(base,funOffset,1,value,true));//지역변수에 값을 저장하고 값이 저장되었다는 true로 해준다.
        }
        //type_spec IDENT '[' LITERAL ']' ';'
        else if(ctx.getChildCount() == 6){
            ldent = ctx.IDENT().getText();//배열의 이름
            literal = ctx.LITERAL().getText();//배열의 크기
            value = Integer.parseInt(literal);//배열의 크기 저장
            //U-코드에서 배열은 size가 1이 아니기때문에 배열의 크기를 size로 출력시킨다.
            newTexts.put(ctx, getSpace() + "sym " + base + " " + funOffset + " " + value+"\n");
            //그 배열의 이름을 키값으로 funMap에 base, offset, size를 저장시킨다
            funMap.put(ldent,new Hash(base,funOffset,value));
            //이함수에 들어올때 offset을 1 증가시켰기 때문에 배열의 size만큼 더해주고 1을 빼준다.
            funOffset = funOffset + value - 1;
        }
    }

    @Override public void enterIf_stmt(MiniCParser.If_stmtContext ctx) {
        ifNum++;
    }
    //끝
    //if문에 대해 처리하는 함수 ex) if (expr) { stmt } , if (expr) { stmt } else { stmt }
    @Override public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
        String expr = "",stmt = "",stmt2 = "";
        expr = newTexts.get(ctx.expr());//if문의 조건문을 저장한 변수
        stmt = newTexts.get(ctx.stmt(0));//if문 안의 코드를 저장한 변수
        //if (expr) { stmt }
        if(ctx.getChildCount() == 5){
            ++labelNum;//if문이 아닌경우에 jump를 해주기위해 1을 증가시켜서 label을 구별해준다.
            String format = "$$" + Integer.toString(labelNum);
            //if문에 대한 U-코드 expr은 expr함수로 이동해서 처리해준다.
            //stmt에 대한 U-코드는 stmt함수로 이동해서 U-코드를 처리해준다.
            newTexts.put(ctx, expr + getSpace() +"fjp " + format + "\n" +
                    stmt + getSpace(format) + "nop\n");
        }
        //if (expr) { stmt } else { stmt }
        else if(ctx.getChildCount() == 7 && ctx.getChild(5) == ctx.ELSE()){

            String format = "$$" + Integer.toString(++labelNum);//else문으로 jump해주기 위해 1을 증가시켜 구별한다.
            String format2 = "$$" + Integer.toString(++labelNum);//else문 밖으로 jump해주기 위해 1을 증가시켜 구별한다.

            stmt2 = newTexts.get(ctx.stmt(1));//else문에 있는 코드를 저장한 변수
            //if문의 조건문에 대해 expr함수로 이동해 처리해서 U-코드로 출력시킨다.
            //if문이 아니라면 format으로 jump를 해준다. if문의 안에 코드를 stmt함수로 이동해 처리해주고 else문 밖으로 jump
            //해주는 label을 출력시켜준다.
            //else문에 대한 label을 출력시키고 다음으로 else문에 대한 코드를 stmt함수로 이동해 처리하고
            //else문의 밖으로 jump되는 label을 출력시킨다.
            newTexts.put(ctx, expr + getSpace() +"fjp " + format + "\n" +
                    stmt + getSpace() +format2 + "\n" +
                    getSpace(format) + "nop\n" + stmt2 + getSpace(format2) + "nop\n");
        }
    }
    //프로그램에 하나라도 return이 있는지 확인하기 위해 retNum = 1로 만든다.
    @Override public void enterReturn_stmt(MiniCParser.Return_stmtContext ctx) {
        retNum = 1;
    }
    //끝
    //return에 대한 함수 ex) return ; , return expr ;
    @Override public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
        String expr = "";
        ret = 1;//함수안에 return이 존재하는지 확인하기 위해 만든 전역변수
        //retrun ;인경우 함수 마지막에 ret을 출력시키게 해준다.
        if(ctx.getChildCount() == 2){
            newTexts.put(ctx, getSpace() + "ret\n");
        }
        //return expr;인 경우에 함수 마지막에 expr에 대해서 expr함수에서 처리하게 하고 retv를 출력시킨다.
        else if(ctx.getChildCount() == 3){
            expr = newTexts.get(ctx.expr());
            newTexts.put(ctx, expr + getSpace() + "retv\n" );

        }
    }

    @Override public void enterExpr(MiniCParser.ExprContext ctx) {

    }

    @Override public void exitExpr(MiniCParser.ExprContext ctx) {
        String op1 = "", op2 = "", ident = "", expr = "", expr2 = "";

        if(ctx.getChildCount() == 1 && ctx.getChildCount() != ';'){
            expr = ctx.getChild(0).getText();
            char check;//문자인지 숫자인지 구별해주기 위해 만든 변수
            check = expr.charAt(expr.length()-1);//첫번째 글자로 문자인지 숫자인지 구별한다.
            if(check < 48 || check > 58){//문자
                if(funMap.get(expr) == null){ //전역변수로 저장된 경우 hash를 전역변수를 저장한것을 이용해야하기에 구별해준다.
                    if(globalMap.get(expr).size == 1) {//전역변수가 배열이 아닌경우
                        //추가
                        if(globalMap.get(expr).literalBool == true){//변수에 값이 저장되어 있으면 값으로 U-코드를 출력시킨다.
                            newTexts.put(ctx, getSpace() + "ldc" + " " +globalMap.get(expr).literal + "\n");
                            result = funMap.get(expr).literal;
                        } else {
                            newTexts.put(ctx, getSpace() + "lod " + globalMap.get(expr).base + " " + globalMap.get(expr).offset + "\n");
                        }
                    } else{//전역변수가 배열인 경우에는 U-코드 lda를 사용해야한다.
                        newTexts.put(ctx, getSpace() + "lda " + globalMap.get(expr).base + " " + globalMap.get(expr).offset + "\n");
                    }
                } else { //함수안에 저장된 경우
                    if(funMap.get(expr).size == 1) {//배열이 아닌경우를 구별해주기위해 사용
                        //추가
                        if(funMap.get(expr).literalBool == true){//변수에 값이 저장되어 있으면 값으로 U-코드를 출력시킨다.
                            newTexts.put(ctx, getSpace() + "ldc" + " " +funMap.get(expr).literal + "\n");
                            result = funMap.get(expr).literal;
                        } else {
                            newTexts.put(ctx, getSpace() + "lod " + base + " " + funMap.get(expr).offset + "\n");
                        }
                    } else if(funMap.get(expr).size > 1){//배열인 경우에는 U-코드 lda를 출력시켜야한다.
                        newTexts.put(ctx, getSpace() + "lda " + base + " " + funMap.get(expr).offset + "\n");
                    }
                }
            } else {//숫자인경우에는 ldc와 그 숫자의 값을 출력시켜야한다.
                newTexts.put(ctx, getSpace()+"ldc " + expr +"\n");
                result = Integer.parseInt(expr);
            }
        }
        //ex) x + y
        else if(isBinaryOperation(ctx)){
            int temp = 0, temp2 = 0;
            op1 = ctx.getChild(1).getText();//operation이 어느것인지 구별하기위해 만든 변수
            String text = "";
            //추가
            //ex) x = 2 + 3; => x = 5; expr이 둘다 숫자인 경우 그 값들을 미리 계산해서 U-코드로 출력시킨다.
            if(isInt(ctx.expr(0).getText()) && isInt(ctx.expr(1).getText())){
                temp = Integer.parseInt(ctx.getChild(0).getText());
                temp2 = Integer.parseInt(ctx.getChild(2).getText());
                if(op1.equals("+")){
                    result = temp + temp2;
                    newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                } else if(op1.equals("-")){
                    result = temp - temp2;
                    newTexts.put(ctx, getSpace() + "ldc " + result  + "\n");
                } else if(op1.equals("*")){
                    result = temp * temp2;
                    newTexts.put(ctx, getSpace() + "ldc " + result  + "\n");
                } else if(op1.equals("/")){
                    result = temp / temp2;
                    newTexts.put(ctx, getSpace() + "ldc " + result  + "\n");
                } else if(op1.equals("%")){
                    result = temp % temp2;
                    newTexts.put(ctx, getSpace() + "ldc " + result  + "\n");
                }
            }else { //추가 expr이 하나만 문자인 경우와 둘 다 문자인 경우
                text = newTexts.get(ctx.expr(0)) +
                        newTexts.get(ctx.expr(1)) + getSpace();//변수들에 대해서는 expr함수를 이용해서 처리해준다.
                if (op1.equals("+")) {
                    //추가
                    //변수에 값이 저장되있으면 그 값을 계산해서 계산된 값만 출력되게 한다.
                    if(isInt(ctx.expr(0).getText()) && isInt(ctx.expr(1).getText()) == false) {//앞이 숫자인경우
                        //문자인지 숫자인지 구별해주는 함수 isInt를 이용해서 앞이 숫자인경우를 찾는다.
                        if(Integer.parseInt(ctx.expr(0).getText()) == 0) {//앞 숫자가 0인경우
                            //하나의 숫자가 0인 경우는 계산하면 나머지 하나의 값만을 출력시키면 되기에 0은 제거한다.
                            text = newTexts.get(ctx.expr(1));
                            result = funMap.get(ctx.expr(1).getText()).literal;
                            newTexts.put(ctx, text);
                        } else{//0이아닌 경우에는 미리 계산을 하고 U-코드로 만들어 준다.
                            temp = Integer.parseInt(ctx.expr(0).getText());
                            temp2 = funMap.get(ctx.expr(1).getText()).literal;
                            result = temp + temp2;
                            newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                        }
                    } else if(isInt(ctx.expr(1).getText()) && isInt(ctx.expr(0).getText()) == false){//뒤가 숫자인경우
                        //문자인지 숫자인지 구별해주는 함수 isInt를 이용해서 뒤가 숫자인 경우를 찾는다.
                        if(Integer.parseInt(ctx.expr(1).getText()) == 0) {//뒤 숫자가 0인경우
                            //하나의 숫자가 0이기 때문에 제거하고 계산해준다.
                            text = newTexts.get(ctx.expr(0));
                            result = funMap.get(ctx.expr(0).getText()).literal;
                            newTexts.put(ctx, text);
                        } else{
                            //0이 아닌경우와 변수에 미리 값이 저장되있는 경우는 값을 가져와 미리 계산을 해준다.
                            temp = Integer.parseInt(ctx.expr(1).getText());
                            temp2 = funMap.get(ctx.expr(0).getText()).literal;
                            result = temp2 + temp;
                            newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                        }
                        //둘다 숫자가 아니고 변수에 값이 저장되 있는 경우 저장되있는 값을 불러와 미리 계산을 하고 U-코드로 만들어 준다.
                    }else if(funMap.get(ctx.expr(0).getText()).literalBool == true && funMap.get(ctx.expr(1).getText()).literalBool == true) {
                        temp = funMap.get(ctx.expr(0).getText()).literal;
                        temp2 = funMap.get(ctx.expr(1).getText()).literal;
                        result = temp + temp2;
                        newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                    } else{
                            newTexts.put(ctx, text + "add\n");
                    }
                } else if (op1.equals("-")) {
                    if(isInt(ctx.expr(0).getText()) && isInt(ctx.expr(1).getText()) == false) {
                        if(Integer.parseInt(ctx.expr(0).getText()) == 0) {
                            text = newTexts.get(ctx.expr(1));
                            result = funMap.get(ctx.expr(1).getText()).literal;
                            newTexts.put(ctx, text);
                        } else{
                            temp = Integer.parseInt(ctx.expr(0).getText());
                            temp2 =funMap.get(ctx.expr(1).getText()).literal;
                            result = temp - temp2;
                            newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                        }
                    } else if(isInt(ctx.expr(1).getText()) && isInt(ctx.expr(0).getText()) == false){
                        if(Integer.parseInt(ctx.expr(1).getText()) == 0) {
                            text = newTexts.get(ctx.expr(0));
                            result = funMap.get(ctx.expr(0).getText()).literal;
                            newTexts.put(ctx, text);
                        } else{
                            temp = Integer.parseInt(ctx.expr(1).getText());
                            temp2 = funMap.get(ctx.expr(0).getText()).literal;
                            result = temp2 - temp;
                            newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                        }
                    }else if(funMap.get(ctx.expr(0).getText()).literalBool == true && funMap.get(ctx.expr(1).getText()).literalBool == true) {
                        temp = funMap.get(ctx.expr(0).getText()).literal;
                        temp2 = funMap.get(ctx.expr(1).getText()).literal;
                        result = temp - temp2;
                        newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                    } else{//추가 0을 더했을 경우에 0을 제거하고 출력한다.
                        newTexts.put(ctx, text + "sub\n");
                    }
                } else if (op1.equals("*")) {
                    //추가
                    if(isInt(ctx.expr(0).getText()) && isInt(ctx.expr(1).getText()) == false) {
                        //* / % 연산에서 하나의 값이 1인 경우에는 어차피 같은 값이 나오기 때문에 1을 제거해서 U-코드로 출력한다.
                        if(Integer.parseInt(ctx.expr(0).getText()) == 1) {
                            text = newTexts.get(ctx.expr(1));
                            result = funMap.get(ctx.expr(1).getText()).literal;
                            newTexts.put(ctx, text);
                        }else if(Integer.parseInt(ctx.expr(0).getText()) == 0){
                            //0을 *해주면 0이 출력된다.
                            result = 0;
                            newTexts.put(ctx, getSpace() + "ldc 0\n");
                        } else{//하나의 값이 숫자이고 다른 하나는 변수인 경우에는 미리 계산해서 U-코드로 출력시킨다.
                            temp = Integer.parseInt(ctx.expr(0).getText());
                            temp2 = funMap.get(ctx.expr(1).getText()).literal;
                            result = temp * temp2;
                            newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                        }
                    } else if(isInt(ctx.expr(1).getText()) && isInt(ctx.expr(0).getText()) == false){
                        if(Integer.parseInt(ctx.expr(1).getText()) == 1) {
                            text = newTexts.get(ctx.expr(0));
                            result = funMap.get(ctx.expr(0).getText()).literal;
                            newTexts.put(ctx, text);
                        }else if(Integer.parseInt(ctx.expr(1).getText()) == 0){
                            result = 0;
                            newTexts.put(ctx, getSpace() + "ldc 0\n");
                        } else{
                            temp = Integer.parseInt(ctx.expr(1).getText());
                            temp2 = funMap.get(ctx.expr(0).getText()).literal;
                            result = temp * temp2;
                            newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                        }
                    }else if(funMap.get(ctx.expr(0).getText()).literalBool == true && funMap.get(ctx.expr(1).getText()).literalBool == true) {
                        //연산에서 두개의 값이 전부 변수이고 값이 저장되있는 경우에 미리 계산을 해서 출력시켜준다.
                        temp = funMap.get(ctx.expr(0).getText()).literal;
                        temp2 = funMap.get(ctx.expr(1).getText()).literal;
                        result = temp * temp2;
                        newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                    } else {
                        newTexts.put(ctx, text + "mult\n");
                    }
                } else if (op1.equals("/")) {
                    if(isInt(ctx.expr(0).getText()) && isInt(ctx.expr(1).getText()) == false) {
                        if(Integer.parseInt(ctx.expr(0).getText()) == 1) {
                            text = newTexts.get(ctx.expr(1));
                            result = funMap.get(ctx.expr(1).getText()).literal;
                            newTexts.put(ctx, text);
                        } else{
                            temp = Integer.parseInt(ctx.expr(0).getText());
                            temp2 = funMap.get(ctx.expr(1).getText()).literal;
                            result = temp / temp2;
                            newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                        }
                    } else if(isInt(ctx.expr(1).getText()) && isInt(ctx.expr(0).getText()) == false){
                        if(Integer.parseInt(ctx.expr(1).getText()) == 1) {
                            text = newTexts.get(ctx.expr(0));
                            result = funMap.get(ctx.expr(0).getText()).literal;
                            newTexts.put(ctx, text);
                        } else{
                            temp = Integer.parseInt(ctx.expr(1).getText());
                            temp2 = funMap.get(ctx.expr(0).getText()).literal;
                            result = temp2 / temp;
                            newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                        }
                    }else if(funMap.get(ctx.expr(0).getText()).literalBool == true && funMap.get(ctx.expr(1).getText()).literalBool == true) {
                        temp = funMap.get(ctx.expr(0).getText()).literal;
                        temp2 = funMap.get(ctx.expr(1).getText()).literal;
                        result = temp / temp2;
                        newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                        } else {
                            newTexts.put(ctx, text + "div\n");
                        }
                } else if (op1.equals("%")) {
                    if(isInt(ctx.expr(0).getText()) && isInt(ctx.expr(1).getText()) == false) {
                        if(Integer.parseInt(ctx.expr(0).getText()) == 1) {
                            text = newTexts.get(ctx.expr(1));
                            result = funMap.get(ctx.expr(1).getText()).literal;
                            newTexts.put(ctx, text);
                        } else{
                            temp = Integer.parseInt(ctx.expr(0).getText());
                            temp2 = funMap.get(ctx.expr(1).getText()).literal;
                            result = temp % temp2;
                            newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                        }
                    } else if(isInt(ctx.expr(1).getText()) && isInt(ctx.expr(0).getText()) == false){
                        if(Integer.parseInt(ctx.expr(1).getText()) == 1) {
                            text = newTexts.get(ctx.expr(0));
                            result = funMap.get(ctx.expr(0).getText()).literal;
                            newTexts.put(ctx, text);
                        } else{
                            temp = Integer.parseInt(ctx.expr(1).getText());
                            temp2 = funMap.get(ctx.expr(0).getText()).literal;
                            result = temp2 % temp;
                            newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                        }
                    }else if(funMap.get(ctx.expr(0).getText()).literalBool == true && funMap.get(ctx.expr(1).getText()).literalBool == true) {
                        temp = funMap.get(ctx.expr(0).getText()).literal;
                        temp2 = funMap.get(ctx.expr(1).getText()).literal;
                        result = temp % temp2;
                        newTexts.put(ctx, getSpace() + "ldc " + result + "\n");
                        } else {
                            newTexts.put(ctx, text + "mod\n");
                        }
                } else if (op1.equals("==")) {
                    newTexts.put(ctx, text + "eq\n");
                } else if (op1.equals("!=")) {
                    newTexts.put(ctx, text + "ne\n");
                } else if (op1.equals("<=")) {
                    newTexts.put(ctx, text + "le\n");
                } else if (op1.equals(">=")) {
                    newTexts.put(ctx, text + "ge\n");
                } else if (op1.equals("and")) {
                    newTexts.put(ctx, text + "and\n");
                } else if (op1.equals("or")) {
                    newTexts.put(ctx, text + "or\n");
                } else if (op1.equals("<")) {
                    newTexts.put(ctx, text + "lt\n");
                } else {
                    newTexts.put(ctx, text + "gt\n");
                }
            }
        }
        //ex) ++x
        else if(isPrefixOperation(ctx)) {
            op1 = ctx.getChild(0).getText();//operation을 구별해주기위한 변수
            op2 = ctx.getChild(1).getText();//expr을 저장하기위해 사용한 변수
            int x = 0, y = 0;
            char check;//문자인지 숫자인지 확인
            check = op2.charAt(op2.length()-1);
            if(check < 48 || check > 58) {//문자
                if (funMap.get(op2) == null) {//funMap에 변수이름이 존재하지 않는다면 전역변수이다.
                    x = globalMap.get(op2).offset;//전역변수의 offset
                    y = globalMap.get(op2).base;//전역변수의 base
                } else {//지역변수,파라미터
                    x = funMap.get(op2).offset;//지역변수,파라미터의 offset
                    y = funMap.get(op2).base;//지역변수,파라미터의 base
                }
                //ex) a = ++b;인 경우에 마지막에 lod를 다시 해주어야 하기에 =을 이용해서 구별해준다.
                if (ctx.getParent().getChild(1).getText().equals("=")) {
                    if (op1.equals("++")) {
                        newTexts.put(ctx, getSpace() + "lod " + y + " " + x + "\n"
                                + getSpace() + "inc\n" + getSpace() + "str " + y + " " + x + "\n" +
                                getSpace() + "lod " + y + " " + x + "\n");
                    } else if (op1.equals("--")) {
                        newTexts.put(ctx, getSpace() + "lod " + y + " " + x + "\n"
                                + getSpace() + "dec\n" + getSpace() + "str " + y + " " + x + "\n" +
                                "lod " + y + " " + x + "\n");
                    } else if (op1.equals("!")) {
                        newTexts.put(ctx, getSpace() + "lod " + y + " " + x + "\n"
                                + getSpace() + "not\n" + "lod " + y + " " + x + "\n");
                    } else if (op1.equals("-")) {
                        newTexts.put(ctx, getSpace() + "lod " + y + " " + x + "\n"
                                + getSpace() + "neg\n" + "lod " + y + " " + x + "\n");
                    }
                } else{//ex) ++a;에 대해 해주기 위해 사용한다.
                    if (op1.equals("++")) {
                        newTexts.put(ctx, getSpace() + "lod " + y + " " + x + "\n"
                                + getSpace() + "inc\n" + getSpace() + "str " + y + " " + x + "\n");
                    } else if (op1.equals("--")) {
                        newTexts.put(ctx, getSpace() + "lod " + y + " " + x + "\n"
                                + getSpace() + "dec\n" + getSpace() + "str " + y + " " + x + "\n");
                    } else if (op1.equals("!")) {
                        newTexts.put(ctx, getSpace() + "lod " + y + " " + x + "\n"
                                + getSpace() + "not\n");
                    } else if (op1.equals("-")) {
                        newTexts.put(ctx, getSpace() + "lod " + y + " " + x + "\n"
                                + getSpace() + "neg\n");
                    }
                }
            } else {//숫자인 경우는 neg밖에 없기에..
                if (op1.equals("-")) {
                    newTexts.put(ctx, getSpace() + "ldc " + op2 + "\n"
                            + getSpace() + "neg\n");
                }
            }
        }
        //ex) x -= 2 안넣어도 된다.
        else if(ctx.getChildCount() == 2 && ctx.getChild(1) != ctx.expr()){
            expr = newTexts.get(ctx.expr(0));
            newTexts.put(ctx, expr);
        }
        //'(' expr ')'인 경우 expr로 이동해서 처리해준다.
        else if(ctx.getChildCount() == 3 && ctx.getChild(1) == ctx.expr()){
            expr = newTexts.get(ctx.expr(0));
            newTexts.put(ctx, expr);
        }

        else if(ctx.getChildCount() == 4){
            ident = ctx.IDENT().getText();
            //IDENT '(' args ')', call에 대한 U-코드를 사용해주기위해 사용
            if(ctx.getChild(2) == ctx.args()){
                expr = newTexts.get(ctx.args());
                newTexts.put(ctx, getSpace()+ "ldp" + "\n" + expr + getSpace() +"call " + ident + "\n");
            }
            //IDENT '[' expr ']'
            else {
                expr = newTexts.get(ctx.expr(0));
                if(funMap.get(ident) == null){//전역변수로 저장된 배열인 경우
                    newTexts.put(ctx, expr +
                            getSpace() +"lda " + globalMap.get(ident).base + " " + globalMap.get(ident).offset + "\n" +
                            getSpace() + "add" + "\n" + getSpace() + "ldi"+"\n");
                } else {
                    if(funMap.get(ident).size == 1) {//파라미터에 저장된 배열인 경우는 lod로 출력시켜야 한다.
                        newTexts.put(ctx, expr +
                                getSpace() + "lod " + base + " " + funMap.get(ident).offset + "\n" +
                                getSpace() + "add" + "\n" + getSpace() + "ldi" + "\n");
                    } else{//함수안에 저장된 배열인 경우는 lda를 U-코드로 출력시켜야한다.
                        newTexts.put(ctx, expr +
                                getSpace() + "lda " + base + " " + funMap.get(ident).offset + "\n" +
                                getSpace() + "add" + "\n" + getSpace() + "ldi" + "\n");
                    }
                }
            }
        }
        //IDENT '[' expr ']' '=' expr
        else if(ctx.getChildCount() == 6){
            ident = ctx.IDENT().getText();
            expr = newTexts.get(ctx.expr(0));
            expr2 = newTexts.get(ctx.expr(1));
            //배열의 값을 선언해주는데 그 배열이 전역변수인경우 globalMap이라는 전역변수를 저장한 Hash에서 값을 가져와야
            //하기에 funMap에 존재하지 않는 경우로 구별해준다.
            if(funMap.get(ident) == null){
                newTexts.put(ctx, expr +
                        getSpace() +"lda " + globalMap.get(ident).base + " " + globalMap.get(ident).offset + "\n" +
                        getSpace() + "add" + "\n" + expr2 + getSpace() + "sti"+"\n");
            }
            //변수가 지역변수와 파라미터로 선언된 경우
            else {
                if (funMap.get(ident).size == 1) {
                    //파라미터에서 선언한 배열의 값을 선언해주는 경우는 lod로 U-코드를 출력시켜주어야한다.
                    //파라미터에 존재하는 배열의 size는 1이기 떄문에 1로 구별해준다.
                    newTexts.put(ctx, expr +
                            getSpace() + "lod " + base + " " + funMap.get(ident).offset + "\n" +
                            getSpace() + "add" + "\n" + expr2 + getSpace() + "sti" + "\n");
                } else {
                    //지역변수로 선언한 배열인 경우는 size가 1이 아니고 U-코드로 lda를 출력해주어야한다.
                    newTexts.put(ctx, expr +
                            getSpace() + "lda " + base + " " + funMap.get(ident).offset + "\n" +
                            getSpace() + "add" + "\n" + expr2 + getSpace() + "sti" + "\n");
                }
            }
        }
        //IDENT '=' expr
        else{
            ident = ctx.IDENT().getText();
            expr = newTexts.get(ctx.expr(0));
            String text = "";
            //변수에 값을  저장해주는데 그 변수가 전역변수인경우 globalMap이라는 전역변수를 저장한 Hash에서 값을 가져와야
            //하기에 funMap에 존재하지 않는 경우로 구별해준다.
            if(funMap.get(ident) == null){//전역변수인경우
                newTexts.put(ctx, expr +
                        getSpace() + "str"+ " " + globalMap.get(ident).base + " " + globalMap.get(ident).offset + "\n");
                //추가 새로 계산된 값을 그 변수의 hash에 저장한다.
                globalMap.put(ident, new Hash(globalMap.get(ident).base,globalMap.get(ident).offset,1,result,true));
            } else {//지역변수인경우
                newTexts.put(ctx, expr +
                        getSpace() + "str" + " " + base + " " + funMap.get(ident).offset + "\n");
                //추가 새로 계산된 값을 그 변수의 hash에 저장한다.
                funMap.put(ident, new Hash(base,funMap.get(ident).offset,1,result,true));
            }
        }
    }

    @Override public void enterArgs(MiniCParser.ArgsContext ctx) {

    }
    //끝
    // expr (',' expr)*
    @Override public void exitArgs(MiniCParser.ArgsContext ctx) {
        if(ctx.getChildCount() > 0){
            String expr = newTexts.get(ctx.expr(0));
            for(int i = 1; i < ctx.expr().size(); i++){
                expr += newTexts.get(ctx.expr(i));
            }
            newTexts.put(ctx, expr);
        }
        //아무것도 없을 경우
        else{
            newTexts.put(ctx, "");
        }
    }

    @Override public void enterEveryRule(ParserRuleContext ctx) {

    }

    @Override public void exitEveryRule(ParserRuleContext ctx) {

    }

    @Override public void visitTerminal(TerminalNode node) {

    }

    @Override public void visitErrorNode(ErrorNode node) {

    }

}