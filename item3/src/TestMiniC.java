/**
 * Created by heebin on 2016-11-07.
 */

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class TestMiniC {
    public static void main(String[] args) throws Exception{

        MiniCLexer lexer = new MiniCLexer( new ANTLRFileStream("test.c"));
        CommonTokenStream tokens  = new CommonTokenStream( lexer );
        MiniCParser parser  = new MiniCParser( tokens );
        ParseTree tree   = parser.program();
        // 여기부터 새로운 부분
        ParseTreeWalker walker  = new ParseTreeWalker();
        walker.walk(new MiniCPrintListener(), tree );

    }
}
