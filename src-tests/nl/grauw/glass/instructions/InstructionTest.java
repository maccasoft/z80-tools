package nl.grauw.glass.instructions;

import static org.junit.Assert.*;

import java.io.LineNumberReader;
import java.io.StringReader;

import nl.grauw.glass.GlobalScope;
import nl.grauw.glass.Line;
import nl.grauw.glass.Parser;
import nl.grauw.glass.Scope;

import org.junit.Test;

public class InstructionTest {
	
	@Test
	public void testAdcA() {
		assertArrayEquals(b(0x88), parse("adc a,b"));
		assertArrayEquals(b(0x89), parse("adc a,c"));
		assertArrayEquals(b(0x8A), parse("adc a,d"));
		assertArrayEquals(b(0x8B), parse("adc a,e"));
		assertArrayEquals(b(0x8C), parse("adc a,h"));
		assertArrayEquals(b(0x8D), parse("adc a,l"));
		assertArrayEquals(b(0x8E), parse("adc a,(hl)"));
		assertArrayEquals(b(0x8F), parse("adc a,a"));
		assertArrayEquals(b(0xDD, 0x8C), parse("adc a,ixh"));
		assertArrayEquals(b(0xFD, 0x8D), parse("adc a,iyl"));
		assertArrayEquals(b(0xDD, 0x8E, 0x47), parse("adc a,(ix + 47H)"));
		assertArrayEquals(b(0xFD, 0x8E, 0x86), parse("adc a,(iy - 7AH)"));
	}
	
	@Test
	public void testAdcAN() {
		assertArrayEquals(b(0xCE, 0x86), parse("adc a,86H"));
	}
	
	@Test
	public void testAdcHL() {
		assertArrayEquals(b(0xED, 0x4A), parse("adc hl,bc"));
		assertArrayEquals(b(0xED, 0x5A), parse("adc hl,de"));
		assertArrayEquals(b(0xED, 0x6A), parse("adc hl,hl"));
		assertArrayEquals(b(0xED, 0x7A), parse("adc hl,sp"));
	}
	
	@Test
	public void testAddA() {
		assertArrayEquals(b(0x80), parse("add a,b"));
		assertArrayEquals(b(0x81), parse("add a,c"));
		assertArrayEquals(b(0x82), parse("add a,d"));
		assertArrayEquals(b(0x83), parse("add a,e"));
		assertArrayEquals(b(0x84), parse("add a,h"));
		assertArrayEquals(b(0x85), parse("add a,l"));
		assertArrayEquals(b(0x86), parse("add a,(hl)"));
		assertArrayEquals(b(0x87), parse("add a,a"));
		assertArrayEquals(b(0xDD, 0x84), parse("add a,ixh"));
		assertArrayEquals(b(0xFD, 0x85), parse("add a,iyl"));
		assertArrayEquals(b(0xDD, 0x86, 0x47), parse("add a,(ix + 47H)"));
		assertArrayEquals(b(0xFD, 0x86, 0x86), parse("add a,(iy - 7AH)"));
	}
	
	@Test
	public void testAddAN() {
		assertArrayEquals(b(0xC6, 0x86), parse("add a,86H"));
	}
	
	@Test
	public void testAddHL() {
		assertArrayEquals(b(0x09), parse("add hl,bc"));
		assertArrayEquals(b(0x19), parse("add hl,de"));
		assertArrayEquals(b(0x29), parse("add hl,hl"));
		assertArrayEquals(b(0x39), parse("add hl,sp"));
		assertArrayEquals(b(0xDD, 0x09), parse("add ix,bc"));
		assertArrayEquals(b(0xDD, 0x19), parse("add ix,de"));
		assertArrayEquals(b(0xDD, 0x29), parse("add ix,ix"));
		assertArrayEquals(b(0xDD, 0x39), parse("add ix,sp"));
		assertArrayEquals(b(0xFD, 0x09), parse("add iy,bc"));
		assertArrayEquals(b(0xFD, 0x19), parse("add iy,de"));
		assertArrayEquals(b(0xFD, 0x29), parse("add iy,iy"));
		assertArrayEquals(b(0xFD, 0x39), parse("add iy,sp"));
	}
	
	@Test
	public void testAnd() {
		assertArrayEquals(b(0xA0), parse("and b"));
		assertArrayEquals(b(0xA1), parse("and c"));
		assertArrayEquals(b(0xA2), parse("and d"));
		assertArrayEquals(b(0xA3), parse("and e"));
		assertArrayEquals(b(0xA4), parse("and h"));
		assertArrayEquals(b(0xA5), parse("and l"));
		assertArrayEquals(b(0xA6), parse("and (hl)"));
		assertArrayEquals(b(0xA7), parse("and a"));
		assertArrayEquals(b(0xDD, 0xA4), parse("and ixh"));
		assertArrayEquals(b(0xFD, 0xA5), parse("and iyl"));
		assertArrayEquals(b(0xDD, 0xA6, 0x47), parse("and (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xA6, 0x86), parse("and (iy - 7AH)"));
	}
	
	@Test
	public void testAndN() {
		assertArrayEquals(b(0xE6, 0x86), parse("and 86H"));
	}
	
	@Test
	public void testBit() {
		assertArrayEquals(b(0xCB, 0x78), parse("bit 7,b"));
		assertArrayEquals(b(0xCB, 0x71), parse("bit 6,c"));
		assertArrayEquals(b(0xCB, 0x6A), parse("bit 5,d"));
		assertArrayEquals(b(0xCB, 0x63), parse("bit 4,e"));
		assertArrayEquals(b(0xCB, 0x5C), parse("bit 3,h"));
		assertArrayEquals(b(0xCB, 0x55), parse("bit 2,l"));
		assertArrayEquals(b(0xCB, 0x4E), parse("bit 1,(hl)"));
		assertArrayEquals(b(0xCB, 0x47), parse("bit 0,a"));
		assertArrayEquals(b(0xDD, 0xCB, 0x47, 0x5E), parse("bit 3,(ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xCB, 0x86, 0x66), parse("bit 4,(iy - 7AH)"));
	}
	
	@Test(expected=ArgumentException.class)
	public void testBit_Invalid() {
		assertArrayEquals(b(0xDD, 0xCB, 0x5C), parse("bit 3,ixh"));
		assertArrayEquals(b(0xFD, 0xCB, 0x55), parse("bit 2,iyl"));
	}
	
	@Test
	public void testCall() {
		assertArrayEquals(b(0xCD, 0x86, 0x47), parse("call 4786H"));
	}
	
	@Test
	public void testCall_F() {
		assertArrayEquals(b(0xC4, 0x86, 0x47), parse("call nz,4786H"));
		assertArrayEquals(b(0xCC, 0x86, 0x47), parse("call z,4786H"));
		assertArrayEquals(b(0xD4, 0x86, 0x47), parse("call nc,4786H"));
		assertArrayEquals(b(0xDC, 0x86, 0x47), parse("call c,4786H"));
		assertArrayEquals(b(0xE4, 0x86, 0x47), parse("call po,4786H"));
		assertArrayEquals(b(0xEC, 0x86, 0x47), parse("call pe,4786H"));
		assertArrayEquals(b(0xF4, 0x86, 0x47), parse("call p,4786H"));
		assertArrayEquals(b(0xFC, 0x86, 0x47), parse("call m,4786H"));
	}
	
	@Test
	public void testCcf() {
		assertArrayEquals(b(0x3F), parse("ccf"));
	}
	
	@Test
	public void testCp() {
		assertArrayEquals(b(0xB8), parse("cp b"));
		assertArrayEquals(b(0xB9), parse("cp c"));
		assertArrayEquals(b(0xBA), parse("cp d"));
		assertArrayEquals(b(0xBB), parse("cp e"));
		assertArrayEquals(b(0xBC), parse("cp h"));
		assertArrayEquals(b(0xBD), parse("cp l"));
		assertArrayEquals(b(0xBE), parse("cp (hl)"));
		assertArrayEquals(b(0xBF), parse("cp a"));
		assertArrayEquals(b(0xDD, 0xBC), parse("cp ixh"));
		assertArrayEquals(b(0xFD, 0xBD), parse("cp iyl"));
		assertArrayEquals(b(0xDD, 0xBE, 0x47), parse("cp (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xBE, 0x86), parse("cp (iy - 7AH)"));
	}
	
	@Test
	public void testCpN() {
		assertArrayEquals(b(0xFE, 0x86), parse("cp 86H"));
	}
	
	@Test
	public void testCpd() {
		assertArrayEquals(b(0xED, 0xA9), parse("cpd"));
	}
	
	@Test
	public void testCpdr() {
		assertArrayEquals(b(0xED, 0xB9), parse("cpdr"));
	}
	
	@Test
	public void testCpi() {
		assertArrayEquals(b(0xED, 0xA1), parse("cpi"));
	}
	
	@Test
	public void testCpir() {
		assertArrayEquals(b(0xED, 0xB1), parse("cpir"));
	}
	
	@Test
	public void testCpl() {
		assertArrayEquals(b(0x2F), parse("cpl"));
	}
	
	@Test
	public void testDaa() {
		assertArrayEquals(b(0x27), parse("daa"));
	}
	
	@Test
	public void testDb() {
		assertArrayEquals(b(0x86), parse("db 86H"));
		assertArrayEquals(b(0x11, 0x22, 0x33, 0x44), parse("db 11H, 22H, 33H, 44H"));
		assertArrayEquals(b(0x11, 0x61, 0x62, 0x63, 0x22), parse("db 11H, \"abc\", 22H"));
	}
	
	@Test
	public void testDd() {
		assertArrayEquals(b(0xCC, 0xDD, 0xEE, 0xFF), parse("dd 0FFEEDDCCH"));
		assertArrayEquals(b(0x00, 0x00, 0x00, 0x80), parse("dd -80000000H"));
		assertArrayEquals(b(0x44, 0x33, 0x22, 0x11, 0x88, 0x77, 0x66, 0x55), parse("dd 11223344H, 55667788H"));
	}
	
	@Test
	public void testDec() {
		assertArrayEquals(b(0x05), parse("dec b"));
		assertArrayEquals(b(0x0D), parse("dec c"));
		assertArrayEquals(b(0x15), parse("dec d"));
		assertArrayEquals(b(0x1D), parse("dec e"));
		assertArrayEquals(b(0x25), parse("dec h"));
		assertArrayEquals(b(0x2D), parse("dec l"));
		assertArrayEquals(b(0x35), parse("dec (hl)"));
		assertArrayEquals(b(0x3D), parse("dec a"));
		assertArrayEquals(b(0xDD, 0x25), parse("dec ixh"));
		assertArrayEquals(b(0xFD, 0x2D), parse("dec iyl"));
		assertArrayEquals(b(0xDD, 0x35, 0x47), parse("dec (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0x35, 0x86), parse("dec (iy - 7AH)"));
	}
	
	@Test
	public void testDec_RR() {
		assertArrayEquals(b(0x0B), parse("dec bc"));
		assertArrayEquals(b(0x1B), parse("dec de"));
		assertArrayEquals(b(0x2B), parse("dec hl"));
		assertArrayEquals(b(0x3B), parse("dec sp"));
		assertArrayEquals(b(0xDD, 0x2B), parse("dec ix"));
		assertArrayEquals(b(0xFD, 0x2B), parse("dec iy"));
	}
	
	@Test
	public void testDi() {
		assertArrayEquals(b(0xF3), parse("di"));
	}
	
	@Test
	public void testDjnz() {
		assertArrayEquals(b(0x10, 0xFE), parse("djnz $"));
	}
	
	@Test
	public void testDs() {
		assertArrayEquals(b(0x00, 0x00, 0x00, 0x00, 0x00), parse("ds 5H"));
		assertArrayEquals(b(0x47, 0x47, 0x47, 0x47, 0x47), parse("ds 5H,47H"));
	}
	
	@Test
	public void testDw() {
		assertArrayEquals(b(0x86, 0x47), parse("dw 4786H"));
		assertArrayEquals(b(0x22, 0x11, 0x44, 0x33, 0x66, 0x55), parse("dw 1122H, 3344H, 5566H"));
		assertArrayEquals(b(0x22, 0x11, 0xAC, 0x20, 0x31, 0x00, 0x30, 0x00), parse("dw 1122H, \"€10\""));
	}
	
	@Test
	public void testEi() {
		assertArrayEquals(b(0xFB), parse("ei"));
	}
	
	@Test
	public void testExAF() {
		assertArrayEquals(b(0x08), parse("ex af,af'"));
	}
	
	@Test
	public void testExDEHL() {
		assertArrayEquals(b(0xEB), parse("ex de,hl"));
	}
	
	@Test
	public void testExSP() {
		assertArrayEquals(b(0xE3), parse("ex (sp),hl"));
		assertArrayEquals(b(0xDD, 0xE3), parse("ex (sp),ix"));
		assertArrayEquals(b(0xFD, 0xE3), parse("ex (sp),iy"));
	}
	
	@Test
	public void testExx() {
		assertArrayEquals(b(0xD9), parse("exx"));
	}
	
	@Test
	public void testHalt() {
		assertArrayEquals(b(0x76), parse("halt"));
	}
	
	@Test
	public void testIm() {
		assertArrayEquals(b(0xED, 0x46), parse("im 0"));
		assertArrayEquals(b(0xED, 0x56), parse("im 1"));
		assertArrayEquals(b(0xED, 0x5E), parse("im 2"));
	}
	
	@Test
	public void testIn_C() {
		assertArrayEquals(b(0xED, 0x40), parse("in b,(c)"));
		assertArrayEquals(b(0xED, 0x48), parse("in c,(c)"));
		assertArrayEquals(b(0xED, 0x50), parse("in d,(c)"));
		assertArrayEquals(b(0xED, 0x58), parse("in e,(c)"));
		assertArrayEquals(b(0xED, 0x60), parse("in h,(c)"));
		assertArrayEquals(b(0xED, 0x68), parse("in l,(c)"));
		assertArrayEquals(b(0xED, 0x70), parse("in (c)"));
		assertArrayEquals(b(0xED, 0x78), parse("in a,(c)"));
	}
	
	@Test
	public void testIn_N() {
		assertArrayEquals(b(0xDB, 0x86), parse("in a,(86H)"));
	}
	
	@Test
	public void testInc() {
		assertArrayEquals(b(0x04), parse("inc b"));
		assertArrayEquals(b(0x0C), parse("inc c"));
		assertArrayEquals(b(0x14), parse("inc d"));
		assertArrayEquals(b(0x1C), parse("inc e"));
		assertArrayEquals(b(0x24), parse("inc h"));
		assertArrayEquals(b(0x2C), parse("inc l"));
		assertArrayEquals(b(0x34), parse("inc (hl)"));
		assertArrayEquals(b(0x3C), parse("inc a"));
		assertArrayEquals(b(0xDD, 0x24), parse("inc ixh"));
		assertArrayEquals(b(0xFD, 0x2C), parse("inc iyl"));
		assertArrayEquals(b(0xDD, 0x34, 0x47), parse("inc (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0x34, 0x86), parse("inc (iy - 7AH)"));
	}
	
	@Test
	public void testInc_RR() {
		assertArrayEquals(b(0x03), parse("inc bc"));
		assertArrayEquals(b(0x13), parse("inc de"));
		assertArrayEquals(b(0x23), parse("inc hl"));
		assertArrayEquals(b(0x33), parse("inc sp"));
		assertArrayEquals(b(0xDD, 0x23), parse("inc ix"));
		assertArrayEquals(b(0xFD, 0x23), parse("inc iy"));
	}
	
	@Test
	public void testInd() {
		assertArrayEquals(b(0xED, 0xAA), parse("ind"));
	}
	
	@Test
	public void testIndr() {
		assertArrayEquals(b(0xED, 0xBA), parse("indr"));
	}
	
	@Test
	public void testIni() {
		assertArrayEquals(b(0xED, 0xA2), parse("ini"));
	}
	
	@Test
	public void testInir() {
		assertArrayEquals(b(0xED, 0xB2), parse("inir"));
	}
	
	@Test
	public void testJp() {
		assertArrayEquals(b(0xC3, 0x86, 0x47), parse("jp 4786H"));
	}
	
	@Test
	public void testJp_F() {
		assertArrayEquals(b(0xC2, 0x86, 0x47), parse("jp nz,4786H"));
		assertArrayEquals(b(0xCA, 0x86, 0x47), parse("jp z,4786H"));
		assertArrayEquals(b(0xD2, 0x86, 0x47), parse("jp nc,4786H"));
		assertArrayEquals(b(0xDA, 0x86, 0x47), parse("jp c,4786H"));
		assertArrayEquals(b(0xE2, 0x86, 0x47), parse("jp po,4786H"));
		assertArrayEquals(b(0xEA, 0x86, 0x47), parse("jp pe,4786H"));
		assertArrayEquals(b(0xF2, 0x86, 0x47), parse("jp p,4786H"));
		assertArrayEquals(b(0xFA, 0x86, 0x47), parse("jp m,4786H"));
	}
	
	@Test
	public void testJp_HL() {
		assertArrayEquals(b(0xE9), parse("jp (hl)"));
		assertArrayEquals(b(0xDD, 0xE9), parse("jp (ix)"));
		assertArrayEquals(b(0xFD, 0xE9), parse("jp (iy)"));
	}
	
	@Test
	public void testJr() {
		assertArrayEquals(b(0x18, 0xFE), parse("jr $"));
	}
	
	@Test
	public void testJr_F() {
		assertArrayEquals(b(0x20, 0xFE), parse("jr nz,$"));
		assertArrayEquals(b(0x28, 0xFE), parse("jr z,$"));
		assertArrayEquals(b(0x30, 0xFE), parse("jr nc,$"));
		assertArrayEquals(b(0x38, 0xFE), parse("jr c,$"));
	}
	
	@Test
	public void testLd_R_R() {
		assertArrayEquals(b(0x78), parse("ld a,b"));
		assertArrayEquals(b(0x71), parse("ld (hl),c"));
		assertArrayEquals(b(0x6A), parse("ld l,d"));
		assertArrayEquals(b(0x63), parse("ld h,e"));
		assertArrayEquals(b(0x5C), parse("ld e,h"));
		assertArrayEquals(b(0x55), parse("ld d,l"));
		assertArrayEquals(b(0x4E), parse("ld c,(hl)"));
		assertArrayEquals(b(0x47), parse("ld b,a"));
		assertArrayEquals(b(0xDD, 0x6C), parse("ld ixl,ixh"));
		assertArrayEquals(b(0xFD, 0x55), parse("ld d,iyl"));
		assertArrayEquals(b(0xDD, 0x6E, 0x47), parse("ld l,(ix + 47H)"));
		assertArrayEquals(b(0xFD, 0x74, 0x86), parse("ld (iy - 7AH),h"));
	}
	
	@Test
	public void testLd_R_N() {
		assertArrayEquals(b(0x06, 0x86), parse("ld b,86H"));
		assertArrayEquals(b(0x0E, 0x86), parse("ld c,86H"));
		assertArrayEquals(b(0x16, 0x86), parse("ld d,86H"));
		assertArrayEquals(b(0x1E, 0x86), parse("ld e,86H"));
		assertArrayEquals(b(0x26, 0x86), parse("ld h,86H"));
		assertArrayEquals(b(0x2E, 0x86), parse("ld l,86H"));
		assertArrayEquals(b(0x36, 0x86), parse("ld (hl),86H"));
		assertArrayEquals(b(0x3E, 0x86), parse("ld a,86H"));
		assertArrayEquals(b(0xDD, 0x26, 0x86), parse("ld ixh,86H"));
		assertArrayEquals(b(0xFD, 0x2E, 0x86), parse("ld iyl,86H"));
		assertArrayEquals(b(0xDD, 0x36, 0x47, 0x86), parse("ld (ix + 47H),86H"));
		assertArrayEquals(b(0xFD, 0x36, 0x86, 0x47), parse("ld (iy - 7AH),47H"));
	}
	
	@Test
	public void testLd_A_IR() {
		assertArrayEquals(b(0xED, 0x57), parse("ld a,i"));
		assertArrayEquals(b(0xED, 0x5F), parse("ld a,r"));
	}
	
	@Test
	public void testLd_IR_A() {
		assertArrayEquals(b(0xED, 0x47), parse("ld i,a"));
		assertArrayEquals(b(0xED, 0x4F), parse("ld r,a"));
	}
	
	@Test
	public void testLd_A_BCDE() {
		assertArrayEquals(b(0x0A), parse("ld a,(bc)"));
		assertArrayEquals(b(0x1A), parse("ld a,(de)"));
	}
	
	@Test
	public void testLd_A_NN() {
		assertArrayEquals(b(0x3A, 0x86, 0x47), parse("ld a,(4786H)"));
	}
	
	@Test
	public void testLd_HL_NN() {
		assertArrayEquals(b(0x2A, 0x86, 0x47), parse("ld hl,(4786H)"));
		assertArrayEquals(b(0xDD, 0x2A, 0x86, 0x47), parse("ld ix,(4786H)"));
		assertArrayEquals(b(0xFD, 0x2A, 0x86, 0x47), parse("ld iy,(4786H)"));
	}
	
	@Test
	public void testLd_RR_NN() {
		assertArrayEquals(b(0xED, 0x4B, 0x86, 0x47), parse("ld bc,(4786H)"));
		assertArrayEquals(b(0xED, 0x5B, 0x86, 0x47), parse("ld de,(4786H)"));
		assertArrayEquals(b(0xED, 0x7B, 0x86, 0x47), parse("ld sp,(4786H)"));
	}
	
	@Test
	public void testLd_BCDE_A() {
		assertArrayEquals(b(0x02), parse("ld (bc),a"));
		assertArrayEquals(b(0x12), parse("ld (de),a"));
	}
	
	@Test
	public void testLd_NN_A() {
		assertArrayEquals(b(0x32, 0x86, 0x47), parse("ld (4786H),a"));
	}
	
	@Test
	public void testLd_NN_HL() {
		assertArrayEquals(b(0x22, 0x86, 0x47), parse("ld (4786H),hl"));
		assertArrayEquals(b(0xDD, 0x22, 0x86, 0x47), parse("ld (4786H),ix"));
		assertArrayEquals(b(0xFD, 0x22, 0x86, 0x47), parse("ld (4786H),iy"));
	}
	
	@Test
	public void testLd_NN_RR() {
		assertArrayEquals(b(0xED, 0x43, 0x86, 0x47), parse("ld (4786H),bc"));
		assertArrayEquals(b(0xED, 0x53, 0x86, 0x47), parse("ld (4786H),de"));
		assertArrayEquals(b(0xED, 0x73, 0x86, 0x47), parse("ld (4786H),sp"));
	}
	
	@Test
	public void testLd_RR_N() {
		assertArrayEquals(b(0x01, 0x86, 0x47), parse("ld bc,4786H"));
		assertArrayEquals(b(0x11, 0x86, 0x47), parse("ld de,4786H"));
		assertArrayEquals(b(0x21, 0x86, 0x47), parse("ld hl,4786H"));
		assertArrayEquals(b(0x31, 0x86, 0x47), parse("ld sp,4786H"));
		assertArrayEquals(b(0xDD, 0x21, 0x86, 0x47), parse("ld ix,4786H"));
		assertArrayEquals(b(0xFD, 0x21, 0x86, 0x47), parse("ld iy,4786H"));
	}
	
	@Test
	public void testLd_SP_HL() {
		assertArrayEquals(b(0xF9), parse("ld sp,hl"));
		assertArrayEquals(b(0xDD, 0xF9), parse("ld sp,ix"));
		assertArrayEquals(b(0xFD, 0xF9), parse("ld sp,iy"));
	}
	
	@Test
	public void testLdd() {
		assertArrayEquals(b(0xED, 0xA8), parse("ldd"));
	}
	
	@Test
	public void testLddr() {
		assertArrayEquals(b(0xED, 0xB8), parse("lddr"));
	}
	
	@Test
	public void testLdi() {
		assertArrayEquals(b(0xED, 0xA0), parse("ldi"));
	}
	
	@Test
	public void testLdir() {
		assertArrayEquals(b(0xED, 0xB0), parse("ldir"));
	}
	
	@Test
	public void testMulub() {
		assertArrayEquals(b(0xED, 0xC1), parse("mulub a,b"));
		assertArrayEquals(b(0xED, 0xC9), parse("mulub a,c"));
		assertArrayEquals(b(0xED, 0xD1), parse("mulub a,d"));
		assertArrayEquals(b(0xED, 0xD9), parse("mulub a,e"));
	}
	
	@Test
	public void testMuluw() {
		assertArrayEquals(b(0xED, 0xC3), parse("muluw hl,bc"));
		assertArrayEquals(b(0xED, 0xF3), parse("muluw hl,sp"));
	}
	
	@Test
	public void testNeg() {
		assertArrayEquals(b(0xED, 0x44), parse("neg"));
	}
	
	@Test
	public void testNop() {
		assertArrayEquals(b(0x00), parse("nop"));
	}
	
	@Test
	public void testOr() {
		assertArrayEquals(b(0xB0), parse("or b"));
		assertArrayEquals(b(0xB1), parse("or c"));
		assertArrayEquals(b(0xB2), parse("or d"));
		assertArrayEquals(b(0xB3), parse("or e"));
		assertArrayEquals(b(0xB4), parse("or h"));
		assertArrayEquals(b(0xB5), parse("or l"));
		assertArrayEquals(b(0xB6), parse("or (hl)"));
		assertArrayEquals(b(0xB7), parse("or a"));
		assertArrayEquals(b(0xDD, 0xB4), parse("or ixh"));
		assertArrayEquals(b(0xFD, 0xB5), parse("or iyl"));
		assertArrayEquals(b(0xDD, 0xB6, 0x47), parse("or (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xB6, 0x86), parse("or (iy - 7AH)"));
	}
	
	@Test
	public void testOrN() {
		assertArrayEquals(b(0xF6, 0x86), parse("or 86H"));
	}
	
	@Test
	public void testOtdr() {
		assertArrayEquals(b(0xED, 0xBB), parse("otdr"));
	}
	
	@Test
	public void testOtir() {
		assertArrayEquals(b(0xED, 0xB3), parse("otir"));
	}
	
	@Test
	public void testOut_C() {
		assertArrayEquals(b(0xED, 0x41), parse("out (c),b"));
		assertArrayEquals(b(0xED, 0x49), parse("out (c),c"));
		assertArrayEquals(b(0xED, 0x51), parse("out (c),d"));
		assertArrayEquals(b(0xED, 0x59), parse("out (c),e"));
		assertArrayEquals(b(0xED, 0x61), parse("out (c),h"));
		assertArrayEquals(b(0xED, 0x69), parse("out (c),l"));
		assertArrayEquals(b(0xED, 0x79), parse("out (c),a"));
	}
	
	@Test
	public void testOut_N() {
		assertArrayEquals(b(0xD3, 0x86), parse("out (86H),a"));
	}
	
	@Test
	public void testOutd() {
		assertArrayEquals(b(0xED, 0xAB), parse("outd"));
	}
	
	@Test
	public void testOuti() {
		assertArrayEquals(b(0xED, 0xA3), parse("outi"));
	}
	
	@Test
	public void testPop() {
		assertArrayEquals(b(0xC1), parse("pop bc"));
		assertArrayEquals(b(0xD1), parse("pop de"));
		assertArrayEquals(b(0xE1), parse("pop hl"));
		assertArrayEquals(b(0xF1), parse("pop af"));
		assertArrayEquals(b(0xDD, 0xE1), parse("pop ix"));
		assertArrayEquals(b(0xFD, 0xE1), parse("pop iy"));
	}
	
	@Test
	public void testPush() {
		assertArrayEquals(b(0xC5), parse("push bc"));
		assertArrayEquals(b(0xD5), parse("push de"));
		assertArrayEquals(b(0xE5), parse("push hl"));
		assertArrayEquals(b(0xF5), parse("push af"));
		assertArrayEquals(b(0xDD, 0xE5), parse("push ix"));
		assertArrayEquals(b(0xFD, 0xE5), parse("push iy"));
	}
	
	@Test
	public void testRes() {
		assertArrayEquals(b(0xCB, 0xB8), parse("res 7,b"));
		assertArrayEquals(b(0xCB, 0xB1), parse("res 6,c"));
		assertArrayEquals(b(0xCB, 0xAA), parse("res 5,d"));
		assertArrayEquals(b(0xCB, 0xA3), parse("res 4,e"));
		assertArrayEquals(b(0xCB, 0x9C), parse("res 3,h"));
		assertArrayEquals(b(0xCB, 0x95), parse("res 2,l"));
		assertArrayEquals(b(0xCB, 0x8E), parse("res 1,(hl)"));
		assertArrayEquals(b(0xCB, 0x87), parse("res 0,a"));
		assertArrayEquals(b(0xDD, 0xCB, 0x47, 0x9E), parse("res 3,(ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xCB, 0x86, 0xA6), parse("res 4,(iy - 7AH)"));
	}
	
	@Test(expected=ArgumentException.class)
	public void testRes_Invalid() {
		assertArrayEquals(b(0xDD, 0xCB, 0x9C), parse("res 3,ixh"));
		assertArrayEquals(b(0xFD, 0xCB, 0x95), parse("res 2,iyl"));
	}
	
	@Test
	public void testRet() {
		assertArrayEquals(b(0xC9), parse("ret"));
	}
	
	@Test
	public void testRetF() {
		assertArrayEquals(b(0xC0), parse("ret nz"));
		assertArrayEquals(b(0xC8), parse("ret z"));
		assertArrayEquals(b(0xD0), parse("ret nc"));
		assertArrayEquals(b(0xD8), parse("ret c"));
		assertArrayEquals(b(0xE0), parse("ret po"));
		assertArrayEquals(b(0xE8), parse("ret pe"));
		assertArrayEquals(b(0xF0), parse("ret p"));
		assertArrayEquals(b(0xF8), parse("ret m"));
	}
	
	@Test
	public void testReti() {
		assertArrayEquals(b(0xED, 0x4D), parse("reti"));
	}
	
	@Test
	public void testRetn() {
		assertArrayEquals(b(0xED, 0x45), parse("retn"));
	}
	
	@Test
	public void testRl() {
		assertArrayEquals(b(0xCB, 0x10), parse("rl b"));
		assertArrayEquals(b(0xCB, 0x11), parse("rl c"));
		assertArrayEquals(b(0xCB, 0x12), parse("rl d"));
		assertArrayEquals(b(0xCB, 0x13), parse("rl e"));
		assertArrayEquals(b(0xCB, 0x14), parse("rl h"));
		assertArrayEquals(b(0xCB, 0x15), parse("rl l"));
		assertArrayEquals(b(0xCB, 0x16), parse("rl (hl)"));
		assertArrayEquals(b(0xCB, 0x17), parse("rl a"));
		assertArrayEquals(b(0xDD, 0xCB, 0x47, 0x16), parse("rl (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xCB, 0x86, 0x16), parse("rl (iy - 7AH)"));
	}
	
	@Test(expected=ArgumentException.class)
	public void testRl_Invalid() {
		assertArrayEquals(b(0xDD, 0xCB, 0x14), parse("rl ixh"));
		assertArrayEquals(b(0xFD, 0xCB, 0x15), parse("rl iyl"));
	}
	
	@Test
	public void testRla() {
		assertArrayEquals(b(0x17), parse("rla"));
	}
	
	@Test
	public void testRlc() {
		assertArrayEquals(b(0xCB, 0x00), parse("rlc b"));
		assertArrayEquals(b(0xCB, 0x01), parse("rlc c"));
		assertArrayEquals(b(0xCB, 0x02), parse("rlc d"));
		assertArrayEquals(b(0xCB, 0x03), parse("rlc e"));
		assertArrayEquals(b(0xCB, 0x04), parse("rlc h"));
		assertArrayEquals(b(0xCB, 0x05), parse("rlc l"));
		assertArrayEquals(b(0xCB, 0x06), parse("rlc (hl)"));
		assertArrayEquals(b(0xCB, 0x07), parse("rlc a"));
		assertArrayEquals(b(0xDD, 0xCB, 0x47, 0x06), parse("rlc (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xCB, 0x86, 0x06), parse("rlc (iy - 7AH)"));
	}
	
	@Test(expected=ArgumentException.class)
	public void testRlc_Invalid() {
		assertArrayEquals(b(0xDD, 0xCB, 0x04), parse("rlc ixh"));
		assertArrayEquals(b(0xFD, 0xCB, 0x05), parse("rlc iyl"));
	}
	
	@Test
	public void testRlca() {
		assertArrayEquals(b(0x07), parse("rlca"));
	}
	
	@Test
	public void testRld() {
		assertArrayEquals(b(0xED, 0x6F), parse("rld"));
	}
	
	@Test
	public void testRr() {
		assertArrayEquals(b(0xCB, 0x18), parse("rr b"));
		assertArrayEquals(b(0xCB, 0x19), parse("rr c"));
		assertArrayEquals(b(0xCB, 0x1A), parse("rr d"));
		assertArrayEquals(b(0xCB, 0x1B), parse("rr e"));
		assertArrayEquals(b(0xCB, 0x1C), parse("rr h"));
		assertArrayEquals(b(0xCB, 0x1D), parse("rr l"));
		assertArrayEquals(b(0xCB, 0x1E), parse("rr (hl)"));
		assertArrayEquals(b(0xCB, 0x1F), parse("rr a"));
		assertArrayEquals(b(0xDD, 0xCB, 0x47, 0x1E), parse("rr (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xCB, 0x86, 0x1E), parse("rr (iy - 7AH)"));
	}
	
	@Test(expected=ArgumentException.class)
	public void testRr_Invalid() {
		assertArrayEquals(b(0xDD, 0xCB, 0x1C), parse("rr ixh"));
		assertArrayEquals(b(0xFD, 0xCB, 0x1D), parse("rr iyl"));
	}
	
	@Test
	public void testRra() {
		assertArrayEquals(b(0x1F), parse("rra"));
	}
	
	@Test
	public void testRrc() {
		assertArrayEquals(b(0xCB, 0x08), parse("rrc b"));
		assertArrayEquals(b(0xCB, 0x09), parse("rrc c"));
		assertArrayEquals(b(0xCB, 0x0A), parse("rrc d"));
		assertArrayEquals(b(0xCB, 0x0B), parse("rrc e"));
		assertArrayEquals(b(0xCB, 0x0C), parse("rrc h"));
		assertArrayEquals(b(0xCB, 0x0D), parse("rrc l"));
		assertArrayEquals(b(0xCB, 0x0E), parse("rrc (hl)"));
		assertArrayEquals(b(0xCB, 0x0F), parse("rrc a"));
		assertArrayEquals(b(0xDD, 0xCB, 0x47, 0x0E), parse("rrc (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xCB, 0x86, 0x0E), parse("rrc (iy - 7AH)"));
	}
	
	@Test(expected=ArgumentException.class)
	public void testRrc_Invalid() {
		assertArrayEquals(b(0xDD, 0xCB, 0x0C), parse("rrc ixh"));
		assertArrayEquals(b(0xFD, 0xCB, 0x0D), parse("rrc iyl"));
	}
	
	@Test
	public void testRrca() {
		assertArrayEquals(b(0x0F), parse("rrca"));
	}
	
	@Test
	public void testRrd() {
		assertArrayEquals(b(0xED, 0x67), parse("rrd"));
	}
	
	@Test
	public void testRst() {
		assertArrayEquals(b(0xC7), parse("rst 00H"));
		assertArrayEquals(b(0xCF), parse("rst 08H"));
		assertArrayEquals(b(0xD7), parse("rst 10H"));
		assertArrayEquals(b(0xDF), parse("rst 18H"));
		assertArrayEquals(b(0xE7), parse("rst 20H"));
		assertArrayEquals(b(0xEF), parse("rst 28H"));
		assertArrayEquals(b(0xF7), parse("rst 30H"));
		assertArrayEquals(b(0xFF), parse("rst 38H"));
	}
	
	@Test
	public void testSbcA() {
		assertArrayEquals(b(0x98), parse("sbc a,b"));
		assertArrayEquals(b(0x99), parse("sbc a,c"));
		assertArrayEquals(b(0x9A), parse("sbc a,d"));
		assertArrayEquals(b(0x9B), parse("sbc a,e"));
		assertArrayEquals(b(0x9C), parse("sbc a,h"));
		assertArrayEquals(b(0x9D), parse("sbc a,l"));
		assertArrayEquals(b(0x9E), parse("sbc a,(hl)"));
		assertArrayEquals(b(0x9F), parse("sbc a,a"));
		assertArrayEquals(b(0xDD, 0x9C), parse("sbc a,ixh"));
		assertArrayEquals(b(0xFD, 0x9D), parse("sbc a,iyl"));
		assertArrayEquals(b(0xDD, 0x9E, 0x47), parse("sbc a,(ix + 47H)"));
		assertArrayEquals(b(0xFD, 0x9E, 0x86), parse("sbc a,(iy - 7AH)"));
	}
	
	@Test
	public void testSbcAN() {
		assertArrayEquals(b(0xDE, 0x86), parse("sbc a,86H"));
	}
	
	@Test
	public void testSbcHL() {
		assertArrayEquals(b(0xED, 0x42), parse("sbc hl,bc"));
		assertArrayEquals(b(0xED, 0x52), parse("sbc hl,de"));
		assertArrayEquals(b(0xED, 0x62), parse("sbc hl,hl"));
		assertArrayEquals(b(0xED, 0x72), parse("sbc hl,sp"));
	}
	
	@Test
	public void testScf() {
		assertArrayEquals(b(0x37), parse("scf"));
	}
	
	@Test
	public void testSet() {
		assertArrayEquals(b(0xCB, 0xF8), parse("set 7,b"));
		assertArrayEquals(b(0xCB, 0xF1), parse("set 6,c"));
		assertArrayEquals(b(0xCB, 0xEA), parse("set 5,d"));
		assertArrayEquals(b(0xCB, 0xE3), parse("set 4,e"));
		assertArrayEquals(b(0xCB, 0xDC), parse("set 3,h"));
		assertArrayEquals(b(0xCB, 0xD5), parse("set 2,l"));
		assertArrayEquals(b(0xCB, 0xCE), parse("set 1,(hl)"));
		assertArrayEquals(b(0xCB, 0xC7), parse("set 0,a"));
		assertArrayEquals(b(0xDD, 0xCB, 0x47, 0xDE), parse("set 3,(ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xCB, 0x86, 0xE6), parse("set 4,(iy - 7AH)"));
	}
	
	@Test(expected=ArgumentException.class)
	public void testSet_Invalid() {
		assertArrayEquals(b(0xDD, 0xCB, 0xDC), parse("set 3,ixh"));
		assertArrayEquals(b(0xFD, 0xCB, 0xD5), parse("set 2,iyl"));
	}
	
	@Test
	public void testSla() {
		assertArrayEquals(b(0xCB, 0x20), parse("sla b"));
		assertArrayEquals(b(0xCB, 0x21), parse("sla c"));
		assertArrayEquals(b(0xCB, 0x22), parse("sla d"));
		assertArrayEquals(b(0xCB, 0x23), parse("sla e"));
		assertArrayEquals(b(0xCB, 0x24), parse("sla h"));
		assertArrayEquals(b(0xCB, 0x25), parse("sla l"));
		assertArrayEquals(b(0xCB, 0x26), parse("sla (hl)"));
		assertArrayEquals(b(0xCB, 0x27), parse("sla a"));
		assertArrayEquals(b(0xDD, 0xCB, 0x47, 0x26), parse("sla (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xCB, 0x86, 0x26), parse("sla (iy - 7AH)"));
	}
	
	@Test(expected=ArgumentException.class)
	public void testSla_Invalid() {
		assertArrayEquals(b(0xDD, 0xCB, 0x24), parse("sla ixh"));
		assertArrayEquals(b(0xFD, 0xCB, 0x25), parse("sla iyl"));
	}
	
	@Test
	public void testSra() {
		assertArrayEquals(b(0xCB, 0x28), parse("sra b"));
		assertArrayEquals(b(0xCB, 0x29), parse("sra c"));
		assertArrayEquals(b(0xCB, 0x2A), parse("sra d"));
		assertArrayEquals(b(0xCB, 0x2B), parse("sra e"));
		assertArrayEquals(b(0xCB, 0x2C), parse("sra h"));
		assertArrayEquals(b(0xCB, 0x2D), parse("sra l"));
		assertArrayEquals(b(0xCB, 0x2E), parse("sra (hl)"));
		assertArrayEquals(b(0xCB, 0x2F), parse("sra a"));
		assertArrayEquals(b(0xDD, 0xCB, 0x47, 0x2E), parse("sra (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xCB, 0x86, 0x2E), parse("sra (iy - 7AH)"));
	}
	
	@Test(expected=ArgumentException.class)
	public void testSra_Invalid() {
		assertArrayEquals(b(0xDD, 0xCB, 0x2C), parse("sra ixh"));
		assertArrayEquals(b(0xFD, 0xCB, 0x2D), parse("sra iyl"));
	}
	
	@Test
	public void testSrl() {
		assertArrayEquals(b(0xCB, 0x38), parse("srl b"));
		assertArrayEquals(b(0xCB, 0x39), parse("srl c"));
		assertArrayEquals(b(0xCB, 0x3A), parse("srl d"));
		assertArrayEquals(b(0xCB, 0x3B), parse("srl e"));
		assertArrayEquals(b(0xCB, 0x3C), parse("srl h"));
		assertArrayEquals(b(0xCB, 0x3D), parse("srl l"));
		assertArrayEquals(b(0xCB, 0x3E), parse("srl (hl)"));
		assertArrayEquals(b(0xCB, 0x3F), parse("srl a"));
		assertArrayEquals(b(0xDD, 0xCB, 0x47, 0x3E), parse("srl (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xCB, 0x86, 0x3E), parse("srl (iy - 7AH)"));
	}
	
	@Test(expected=ArgumentException.class)
	public void testSrl_Invalid() {
		assertArrayEquals(b(0xDD, 0xCB, 0x3C), parse("srl ixh"));
		assertArrayEquals(b(0xFD, 0xCB, 0x3D), parse("srl iyl"));
	}
	
	@Test
	public void testSub() {
		assertArrayEquals(b(0x90), parse("sub b"));
		assertArrayEquals(b(0x91), parse("sub c"));
		assertArrayEquals(b(0x92), parse("sub d"));
		assertArrayEquals(b(0x93), parse("sub e"));
		assertArrayEquals(b(0x94), parse("sub h"));
		assertArrayEquals(b(0x95), parse("sub l"));
		assertArrayEquals(b(0x96), parse("sub (hl)"));
		assertArrayEquals(b(0x97), parse("sub a"));
		assertArrayEquals(b(0xDD, 0x94), parse("sub ixh"));
		assertArrayEquals(b(0xFD, 0x95), parse("sub iyl"));
		assertArrayEquals(b(0xDD, 0x96, 0x47), parse("sub (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0x96, 0x86), parse("sub (iy - 7AH)"));
	}
	
	@Test
	public void testSubN() {
		assertArrayEquals(b(0xD6, 0x86), parse("sub 86H"));
	}
	
	@Test
	public void testXor() {
		assertArrayEquals(b(0xA8), parse("xor b"));
		assertArrayEquals(b(0xA9), parse("xor c"));
		assertArrayEquals(b(0xAA), parse("xor d"));
		assertArrayEquals(b(0xAB), parse("xor e"));
		assertArrayEquals(b(0xAC), parse("xor h"));
		assertArrayEquals(b(0xAD), parse("xor l"));
		assertArrayEquals(b(0xAE), parse("xor (hl)"));
		assertArrayEquals(b(0xAF), parse("xor a"));
		assertArrayEquals(b(0xDD, 0xAC), parse("xor ixh"));
		assertArrayEquals(b(0xFD, 0xAD), parse("xor iyl"));
		assertArrayEquals(b(0xDD, 0xAE, 0x47), parse("xor (ix + 47H)"));
		assertArrayEquals(b(0xFD, 0xAE, 0x86), parse("xor (iy - 7AH)"));
	}
	
	@Test
	public void testXorN() {
		assertArrayEquals(b(0xEE, 0x86), parse("xor 86H"));
	}
	
	public byte[] parse(String string) {
		LineNumberReader reader = new LineNumberReader(new StringReader(" " + string));
		Line line = new Parser().parse(reader, new Scope(new GlobalScope()), null);
		line.resolve(0x4321);
		byte[] bytes = line.getBytes();
		assertEquals(bytes.length, line.getSize());
		return bytes;
	}
	
	public byte[] b(int... values) {
		byte[] bytes = new byte[values.length];
		for (int i = 0; i < values.length; i++)
			bytes[i] = (byte)values[i];
		return bytes;
	}
	
}
