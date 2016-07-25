package de.neemann.assembler.asm.formatter;

import de.neemann.assembler.asm.Instruction;
import de.neemann.assembler.asm.InstructionVisitor;
import de.neemann.assembler.asm.MachineCodeListener;
import de.neemann.assembler.asm.Opcode;
import de.neemann.assembler.expression.Context;
import de.neemann.assembler.expression.ExpressionException;

import java.io.PrintStream;

/**
 * Visitor to format a {@link de.neemann.assembler.asm.Program}
 *
 * @author hneemann
 */
public class AsmFormatter implements InstructionVisitor {
    private final PrintStream o;
    private final int columnOfs;
    private final boolean includeLineNumbers;
    private final boolean indentCreated;
    private int actCol;

    /**
     * Creates a new instance
     *
     * @param out thr stream to write to
     */
    public AsmFormatter(PrintStream out) {
        this(out, true);
    }

    /**
     * Creates a new instance
     *
     * @param out                thr stream to write to
     * @param includeLineNumbers true to include the lin number to the output
     */
    public AsmFormatter(PrintStream out, boolean includeLineNumbers) {
        this.o = out;
        this.includeLineNumbers = includeLineNumbers;

        if (includeLineNumbers)
            columnOfs = 0;
        else
            columnOfs = 6;

        actCol = 0;

        indentCreated = true;
    }

    @Override
    public void visit(Instruction i, Context context) throws ExpressionException {
        if (i.getComment() != null)
            o.println(i.getComment());


        boolean labelIsPrinted = false;
        if (i.getLabel() != null && i.getLabel().length() > 8) {
            tab(22);
            print(i.getLabel() + ":");
            newLine();
            labelIsPrinted = true;
        }

        if (i.getMacroDescription() != null) {
            tab(32);
            print(i.getMacroDescription());
            newLine();
        }

        if (includeLineNumbers) {
            if (i.getLineNumber() > 0) {
                print(Integer.toString(i.getLineNumber()));
            }
            tab(3);
            print(" | ");
        }

        printHex(context.getInstrAddr());

        print(": ");

        i.createMachineCode(context, new MachineCodeListener() {
            @Override
            public void add(int instr) {
                printHex(instr);
                print(" ");
            }
        });

        if (!labelIsPrinted) {
            tab(22);
            if (i.getLabel() != null)
                print(i.getLabel() + ":");
        }

        int ofs = 0;
        if (indentCreated && isCreated(i))
            ofs = 1;

        tab(32 + ofs);

        Opcode opcode = i.getOpcode();
        print(opcode.name());

        tab(38 + ofs);

        print(opcode.getArguments().format(i));
        if (i.getConstant() != null) {
            tab(55);
            print("; 0x");
            print(Integer.toHexString(i.getConstant().getValue(context) & 0xffff));
        }
        newLine();
    }

    private boolean isCreated(Instruction i) {
        return i.getLineNumber() == 0 || i.getMacroDescription() != null;
    }

    private void printHex(int instr) {
        String s = Integer.toHexString(instr);
        while (s.length() < 4) s = '0' + s;
        print(s);
    }

    private void tab(int col) {
        col -= columnOfs;

        while (actCol < col)
            print(" ");
    }

    private void print(String s) {
        o.print(s);
        actCol += s.length();
    }

    private void newLine() {
        o.print('\n');
        actCol = 0;
    }
}
