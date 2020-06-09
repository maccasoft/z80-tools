;==================================================================================
; Contents of this file are copyright Grant Searle
; HEX routines from Joel Owens.
;
; You have permission to use this for NON COMMERCIAL USE ONLY
; If you wish to use it elsewhere, please include an acknowledgement to myself.
;
; http://searle.hostei.com/grant/index.html
;
; eMail: home.micros01@btinternet.com
;
; If the above don't work, please perform an Internet search to see if I have
; updated the web page hosting service.
;
;==================================================================================

;------------------------------------------------------------------------------
;
; Z80 Monitor Rom
;
;------------------------------------------------------------------------------
; General Equates
;------------------------------------------------------------------------------

CR              .EQU  0DH
LF              .EQU  0AH
ESC             .EQU  1BH
CTRLC           .EQU  03H
CLS             .EQU  0CH

; CF registers
CF_DATA         .EQU  10H
CF_FEATURES     .EQU  11H
CF_ERROR        .EQU  11H
CF_SECCOUNT     .EQU  12H
CF_SECTOR       .EQU  13H
CF_CYL_LOW      .EQU  14H
CF_CYL_HI       .EQU  15H
CF_HEAD         .EQU  16H
CF_STATUS       .EQU  17H
CF_COMMAND      .EQU  17H
CF_LBA0         .EQU  13H
CF_LBA1         .EQU  14H
CF_LBA2         .EQU  15H
CF_LBA3         .EQU  16H

;CF Features
CF_8BIT         .EQU  1
CF_NOCACHE      .EQU  82H
;CF Commands
CF_READ_SEC     .EQU  20H
CF_WRITE_SEC    .EQU  30H
CF_SET_FEAT     .EQU  0EFH

;BASIC cold and warm entry points
BASCLD          .EQU  2000H
BASWRM          .EQU  2003H

LOADADDR        .EQU  0D000H                ; CP/M load address
NUMSECS         .EQU  24                    ; Number of 512 sectors to be loaded

SIOA_D          .EQU  81H
SIOA_C          .EQU  80H
SIOB_D          .EQU  83H
SIOB_C          .EQU  82H

;------------------------------------------------------------------------------
;                         START OF MONITOR ROM
;------------------------------------------------------------------------------

                .ORG  00H                   ; MONITOR ROM RESET VECTOR

;------------------------------------------------------------------------------
; Reset
;------------------------------------------------------------------------------

RST00           DI                          ; Disable INTerrupts
                JP    INIT                  ; Initialize Hardware and go
                NOP
                NOP
                NOP
                NOP

;------------------------------------------------------------------------------
; TX a character over RS232 wait for TXDONE first.
;------------------------------------------------------------------------------

RST08           JP    CONOUT
                NOP
                NOP
                NOP
                NOP
                NOP

;------------------------------------------------------------------------------
; RX a character from buffer wait until char ready.
;------------------------------------------------------------------------------

RST10           JP    CONIN
                NOP
                NOP
                NOP
                NOP
                NOP

;------------------------------------------------------------------------------
; Check input buffer status
;------------------------------------------------------------------------------

RST18           JP    CKINCHAR
                NOP
                NOP
                NOP
                NOP
                NOP

;------------------------------------------------------------------------------
; Interrupt vector
;------------------------------------------------------------------------------

                .ORG  38H

                RETI
                NOP
                NOP
                NOP
                NOP
                NOP
                NOP

;------------------------------------------------------------------------------
; Console input routine
; Use the "primaryIO" flag to determine which input port to monitor.
;------------------------------------------------------------------------------

CONIN
                LD    A, (PRIMARYIO)
                CP    00H
                JR    NZ, CONINB

CONINA
                XOR   A
                OUT   (SIOA_C), A
WAITINA         IN    A, (SIOA_C)           ; Status byte D2=TX Buff Empty, D0=RX char ready
                AND   01H                   ; Rotates RX status into Carry Flag,
                JR    Z, WAITINA
                IN    A, (SIOA_D)
                RET                         ; Char ready in A

CONINB
                XOR   A
                OUT   (SIOB_C), A
WAITINB         IN    A, (SIOB_C)           ; Status byte D2=TX Buff Empty, D0=RX char ready
                AND   01H                   ; Rotates RX status into Carry Flag,
                JR    Z, WAITINB
                IN    A, (SIOB_D)
                RET                         ; Char ready in A

;------------------------------------------------------------------------------
; Console output routine
; Use the "primaryIO" flag to determine which output port to send a character.
;------------------------------------------------------------------------------

CONOUT
                PUSH  AF                    ; Store character
                LD    A, (PRIMARYIO)
                CP    00H
                JR    NZ, CONOUTB1
                JR    CONOUTA1

CONOUTA
                PUSH  AF
CONOUTA1        XOR   A                     ; See if SIO channel A is finished transmitting
                OUT   (SIOA_C), A
WAITOUTA        IN    A, (SIOA_C)           ; Status byte D2=TX Buff Empty, D0=RX char ready
                AND   04H
                JR    Z, WAITOUTA           ; Loop until SIO flag signals ready
                POP   AF                    ; RETrieve character
                OUT   (SIOA_D), A           ; OUTput the character
                RET

CONOUTB
                PUSH  AF
CONOUTB1        XOR   A                     ; See if SIO channel B is finished transmitting
                OUT   (SIOB_C), A
WAITOUTB        IN    A, (SIOB_C)           ; Status byte D2=TX Buff Empty, D0=RX char ready
                AND   04H
                JR    Z, WAITOUTB           ; Loop until SIO flag signals ready
                POP   AF                    ; RETrieve character
                OUT   (SIOB_D), A           ; OUTput the character
                RET

;------------------------------------------------------------------------------
; Check if there is a character in the input buffer
; Use the "primaryIO" flag to determine which port to check.
;------------------------------------------------------------------------------

CKINCHAR
                LD    A, (PRIMARYIO)
                CP    00H
                JR    NZ, CKINCHARB

CKINCHARA       XOR   A                     ; See if SIO channel A is finished transmitting
                OUT   (SIOA_C), A
                IN    A, (SIOA_C)           ; Status byte D2=TX Buff Empty, D0=RX char ready
                AND   01H
                RET

CKINCHARB       XOR   A
                OUT   (SIOB_C), A
                IN    A, (SIOB_C)           ; Status byte D2=TX Buff Empty, D0=RX char ready
                AND   01H
                RET

;------------------------------------------------------------------------------
; Filtered Character I/O
;------------------------------------------------------------------------------

RDCHR
                RST   10H
                CP    LF
                JR    Z, RDCHR              ; Ignore LF
                CP    ESC
                JR    NZ, RDCHR1
                LD    A, CTRLC              ; Change ESC to CTRL-C
RDCHR1          RET

WRCHR           CP    CR
                JR    Z, WRCRLF             ; When CR, write CRLF
                CP    CLS
                JR    Z, WR                 ; Allow write of "CLS"
                CP    ' '                   ; Don't write out any other control codes
                JR    C, NOWR               ; ie. < space
WR              RST   08H
NOWR            RET

WRCRLF          LD    A, CR
                RST   08H
                LD    A, LF
                RST   08H
                LD    A, CR
                RET

;------------------------------------------------------------------------------
; Initialise hardware and start main loop
;------------------------------------------------------------------------------
INIT
                LD    SP, STACK             ; Set the Stack Pointer

                ; Initialise SIO A

                LD    A, 00H
                OUT   (SIOA_C), A
                LD    A, 18H
                OUT   (SIOA_C), A

                LD    A, 04H
                OUT   (SIOA_C), A
                LD    A, 0C4H
                OUT   (SIOA_C), A

                LD    A, 01H
                OUT   (SIOA_C), A
                LD    A, 00H
                OUT   (SIOA_C), A

                LD    A, 03H
                OUT   (SIOA_C), A
                LD    A, 0E1H
                OUT   (SIOA_C), A

                LD    A, 05H
                OUT   (SIOA_C), A
                LD    A, 0EAH
                OUT   (SIOA_C), A

                ; Initialise SIO B

                LD    A, 00H
                OUT   (SIOB_C), A
                LD    A, 18H
                OUT   (SIOB_C), A

                LD    A, 04H
                OUT   (SIOB_C), A
                LD    A, 0C4H
                OUT   (SIOB_C), A

                LD    A, 01H
                OUT   (SIOB_C), A
                LD    A, 00H
                OUT   (SIOB_C), A

                LD    A, 02H
                OUT   (SIOB_C), A
                LD    A, 00H
                OUT   (SIOB_C), A

                LD    A, 03H
                OUT   (SIOB_C), A
                LD    A, 0E1H
                OUT   (SIOB_C), A

                LD    A, 05H
                OUT   (SIOB_C), A
                LD    A, 0EAH
                OUT   (SIOB_C), A

                ; Set primary console and clear screen

                LD    A, 00H
                LD    (PRIMARYIO), A
                LD    A, CLS
                RST   08H

;------------------------------------------------------------------------------
; Monitor
;------------------------------------------------------------------------------

                CALL  TXCRLF                ; TXCRLF
                LD    HL, SIGNON            ; Print SIGNON message
                CALL  PRINT

                ; Command loop

MAIN
                LD    HL, MAIN              ; Save entry point for Monitor
                PUSH  HL                    ; This is the return address
MAIN0           CALL  TXCRLF                ; Entry point for Monitor, Normal
                LD    A, '>'                ; Get a ">"
                RST   08H                   ; print it

MAIN1           CALL  RDCHR                 ; Get a character from the input port
                CP    ' '                   ; <spc> or less?
                JR    C, MAIN1              ; Go back

                CP    ':'                   ; ":"?
                JP    Z, LOAD               ; First character of a HEX load

                CALL  WRCHR                 ; Print char on console

                CP    '?'
                JP    Z, HELP

                AND   5FH                   ; Make character uppercase

                CP    'R'
                JP    Z, RST00

                CP    'B'
                JP    Z, BASIC

                CP    'G'
                JP    Z, GOTO

                CP    'X'
                JP    Z, CPMLOAD

                LD    A, '?'                ; Get a "?"
                RST   08H                   ; Print it
                JR    MAIN0

;------------------------------------------------------------------------------
; Print string of characters to Serial A until byte=$00, WITH CR, LF
;------------------------------------------------------------------------------

PRINT
                LD    A, (HL)               ; Get character
                OR    A                     ; Is it $00 ?
                RET   Z                     ; Then RETurn on terminator
                RST   08H                   ; Print it
                INC   HL                    ; Next Character
                JR    PRINT                 ; Continue until $00

TXCRLF
                LD    A, 0DH                ;
                RST   08H                   ; Print character
                LD    A, 0AH                ;
                RST   08H                   ; Print character
                RET

;------------------------------------------------------------------------------
; Get a character from the console, must be $20-$7F to be valid (no control characters)
; <Ctrl-c> and <SPACE> breaks with the Zero Flag set
;------------------------------------------------------------------------------

GETCHR
                CALL  RDCHR                 ; RX a Character
                CP    03H                   ; <ctrl-c> User break?
                RET   Z
                CP    20H                   ; <space> or better?
                JR    C, GETCHR             ; Do it again until we get something usable
                RET

;------------------------------------------------------------------------------
; Gets two ASCII characters from the console (assuming them to be HEX 0-9 A-F)
; Moves them into B and C, converts them into a byte value in A and updates a
; Checksum value in E
;------------------------------------------------------------------------------

GET2
                CALL  GETCHR                ; Get us a valid character to work with
                LD    B, A                  ; Load it in B
                CALL  GETCHR                ; Get us another character
                LD    C, A                  ; load it in C
                CALL  BCTOA                 ; Convert ASCII to byte
                LD    C, A                  ; Build the checksum
                LD    A, E
                SUB   C                     ; The checksum should always equal zero when checked
                LD    E, A                  ; Save the checksum back where it came from
                LD    A, C                  ; Retrieve the byte and go back
                RET

;------------------------------------------------------------------------------
; Gets four Hex characters from the console, converts them to values in HL
;------------------------------------------------------------------------------

GETHL
                LD    HL, 00H               ; Gets xxxx but sets Carry Flag on any Terminator
                CALL  ECHO                  ; RX a Character
                CP    0DH                   ; <CR>?
                JR    NZ, GETX2             ; other key
SETCY           SCF                         ; Set Carry Flag
                RET                         ; and Return to main program

;------------------------------------------------------------------------------
; This routine converts last four hex characters (0-9 A-F) user types into a value in HL
; Rotates the old out and replaces with the new until the user hits a terminating character
;------------------------------------------------------------------------------

GETX
                LD    HL, 00H               ; CLEAR HL
GETX1           CALL  ECHO                  ; RX a character from the console
                CP    0DH                   ; <CR>
                RET   Z                     ; quit
                CP    2CH                   ; <,> can be used to safely quit for multiple entries
                RET   Z                     ; (Like filling both DE and HL from the user)
GETX2           CP    03H                   ; Likewise, a <ctrl-C> will terminate clean, too, but
                JR    Z, SETCY              ; It also sets the Carry Flag for testing later.
                ADD   HL, HL                ; Otherwise, rotate the previous low nibble to high
                ADD   HL, HL                ; rather slowly
                ADD   HL, HL                ; until we get to the top
                ADD   HL, HL                ; and then we can continue on.
                SUB   30H                   ; Convert ASCII to byte value
                CP    0AH                   ; Are we in the 0-9 range?
                JR    C, GETX3              ; Then we just need to sub $30, but if it is A-F
                SUB   07H                   ; We need to take off 7 more to get the value down to
GETX3           AND   0FH                   ; to the right hex value
                ADD   A, L                  ; Add the high nibble to the low
                LD    L, A                  ; Move the byte back to A
                JR    GETX1                 ; and go back for next character until he terminates

;------------------------------------------------------------------------------
; Convert ASCII characters in B C registers to a byte value in A
;------------------------------------------------------------------------------

BCTOA           LD    A, B                  ; Move the hi order byte to A
                SUB   30H                   ; Take it down from Ascii
                CP    0AH                   ; Are we in the 0-9 range here?
                JR    C, BCTOA1             ; If so, get the next nybble
                SUB   07H                   ; But if A-F, take it down some more
BCTOA1          RLCA                        ; Rotate the nybble from low to high
                RLCA                        ; One bit at a time
                RLCA                        ; Until we
                RLCA                        ; Get there with it
                LD    B, A                  ; Save the converted high nybble
                LD    A, C                  ; Now get the low order byte
                SUB   30H                   ; Convert it down from Ascii
                CP    0AH                   ; 0-9 at this point?
                JR    C, BCTOA2             ; Good enough then, but
                SUB   07H                   ; Take off 7 more if it's A-F
BCTOA2          ADD   A, B                  ; Add in the high order nybble
                RET

;------------------------------------------------------------------------------
; Get a character and echo it back to the user
;------------------------------------------------------------------------------

ECHO
                CALL  RDCHR
                CALL  WRCHR
                RET

;------------------------------------------------------------------------------
; GOTO command
;------------------------------------------------------------------------------

GOTO
                CALL  GETHL                 ; ENTRY POINT FOR <G>oto addr. Get XXXX from user.
                RET   C                     ; Return if invalid
                LD    A, (PRIMARYIO)
                PUSH  HL
                RET                         ; Jump to HL address value

;------------------------------------------------------------------------------
; LOAD Intel Hex format file from the console.
; [Intel Hex Format is:
; 1) Colon (Frame 0)
; 2) Record Length Field (Frames 1 and 2)
; 3) Load Address Field (Frames 3,4,5,6)
; 4) Record Type Field (Frames 7 and 8)
; 5) Data Field (Frames 9 to 9+2*(Record Length)-1
; 6) Checksum Field - Sum of all byte values from Record Length to and
;   including Checksum Field = 0 ]
;------------------------------------------------------------------------------

LOAD
                LD    E, 0                  ; First two Characters is the Record Length Field
                CALL  GET2                  ; Get us two characters into BC, convert it to a byte <A>
                LD    D, A                  ; Load Record Length count into D
                CALL  GET2                  ; Get next two characters, Memory Load Address <H>
                LD    H, A                  ; put value in H register.
                CALL  GET2                  ; Get next two characters, Memory Load Address <L>
                LD    L, A                  ; put value in L register.
                CALL  GET2                  ; Get next two characters, Record Field Type
                CP    01H                   ; Record Field Type 00 is Data, 01 is End of File
                JR    NZ, LOAD2             ; Must be the end of that file
                CALL  GET2                  ; Get next two characters, assemble into byte
                LD    A, E                  ; Recall the Checksum byte
                AND   A                     ; Is it Zero?
                JR    Z, LOAD00             ; Print footer reached message
                JR    LOADERR               ; Checksums don't add up, Error out

LOAD2           LD    A, D                  ; Retrieve line character counter
                AND   A                     ; Are we done with this line?
                JR    Z, LOAD3              ; Get two more ascii characters, build a byte and checksum
                CALL  GET2                  ; Get next two chars, convert to byte in A, checksum it
                LD    (HL), A               ; Move converted byte in A to memory location
                INC   HL                    ; Increment pointer to next memory location
                LD    A, '.'                ; Print out a "." for every byte loaded
                RST   08H                   ;
                DEC   D                     ; Decrement line character counter
                JR    LOAD2                 ; and keep loading into memory until line is complete

LOAD3           CALL  GET2                  ; Get two chars, build byte and checksum
                LD    A, E                  ; Check the checksum value
                AND   A                     ; Is it zero?
                RET   Z

LOADERR         LD    HL, CKSUMERR          ; Get "Checksum Error" message
                CALL  PRINT                 ; Print Message from (HL) and terminate the load
                RET

LOAD00          LD    HL, LDETXT            ; Print load complete message
                CALL  PRINT
                RET

;------------------------------------------------------------------------------
; Display Help command
;------------------------------------------------------------------------------

HELP
                LD    HL, HLPTXT            ; Print Help message
                CALL  PRINT
                RET

;------------------------------------------------------------------------------
; Start BASIC command
;------------------------------------------------------------------------------

BASIC
                LD    HL, BASTXT
                CALL  PRINT
                CALL  GETCHR
                RET   Z                     ; Cancel if CTRL-C
                AND   5FH                   ; uppercase
                CP    'C'
                JP    Z, BAS1
                CP    'W'
                JP    Z, BAS2
                RET

BASTXT
                .BYTE 0DH, 0AH
                .TEXT "Cold or Warm ?"
                .BYTE 00H

BAS1            LD    A, CLS
                RST   08
                JP    BASCLD

BAS2            CALL  TXCRLF
                CALL  TXCRLF
                JP    BASWRM

;------------------------------------------------------------------------------
; CP/M load command
;------------------------------------------------------------------------------

CPMLOAD
                LD    HL, CPMTXT
                CALL  PRINT
                CALL  GETCHR
                RET   Z                     ; Cancel if CTRL-C
                AND   5FH                   ; uppercase
                CP    'Y'
                JP    Z, CPMLOAD2
                RET

CPMTXT          .BYTE 0DH, 0AH
                .TEXT "Boot CP/M?"
                .BYTE 00H

CPMTXT2         .BYTE 0DH, 0AH
                .TEXT "Loading CP/M..."
                .BYTE 0DH, 0AH, 00H

CPMLOAD2
                LD    HL, CPMTXT2
                CALL  PRINT

                CALL  CFWAIT
                LD    A, CF_8BIT            ; Set IDE to be 8bit
                OUT   (CF_FEATURES), A
                LD    A, CF_SET_FEAT
                OUT   (CF_COMMAND), A

                CALL  CFWAIT
                LD    A, CF_NOCACHE         ; No write cache
                OUT   (CF_FEATURES), A
                LD    A, CF_SET_FEAT
                OUT   (CF_COMMAND), A

                LD    B, NUMSECS

                LD    A, 0
                LD    (SECNO), A
                LD    HL, LOADADDR
                LD    (DMAADDR), HL

PROCESSSECTORS
                CALL  CFWAIT

                LD    A, (SECNO)
                OUT   (CF_LBA0), A
                LD    A, 0
                OUT   (CF_LBA1), A
                OUT   (CF_LBA2), A
                LD    A, 0E0H
                OUT   (CF_LBA3), A
                LD    A, 1
                OUT   (CF_SECCOUNT), A

                CALL  READ

                LD    DE, 0200H
                LD    HL, (DMAADDR)
                ADD   HL, DE
                LD    (DMAADDR), HL
                LD    A, (SECNO)
                INC   A
                LD    (SECNO), A

                DJNZ  PROCESSSECTORS

; Start CP/M using entry at top of BIOS
; The current active console stream ID is pushed onto the stack
; to allow the CBIOS to pick it up
; 0 = SIO A, 1 = SIO B

                LD    A, (PRIMARYIO)
                PUSH  AF
                LD    HL, (0FFFEH)
                JP    (HL)

;------------------------------------------------------------------------------

; Read physical sector from host

READ
                PUSH  AF
                PUSH  BC
                PUSH  HL

                CALL  CFWAIT

                LD    A, CF_READ_SEC
                OUT   (CF_COMMAND), A

                CALL  CFWAIT

                LD    C, 4
                LD    HL, (DMAADDR)
RD4SECS
                LD    B, 128
RDBYTE
                NOP
                NOP
                IN    A, (CF_DATA)
                LD    (HL), A
                INC   HL
                DEC   B
                JR    NZ, RDBYTE
                DEC   C
                JR    NZ, RD4SECS

                POP   HL
                POP   BC
                POP   AF

                RET


; Wait for disk to be ready (busy=0,ready=1)

CFWAIT
TSTBUSY         IN    A, (CF_STATUS)
                AND   80H
                JR    NZ, TSTBUSY
TSTREADY        IN    A, (CF_STATUS)
                AND   40H
                JR    Z, TSTREADY
                RET

;------------------------------------------------------------------------------

SIGNON          .BYTE "Z80 SBC Boot ROM 1.1"
                .BYTE " by G. Searle"
                .BYTE 0DH, 0AH
                .BYTE "Type ? for options"
                .BYTE 0DH, 0AH, 00H

CKSUMERR        .BYTE "Checksum error"
                .BYTE 0DH, 0AH, 00H

LDETXT          .TEXT "Load complete."
                .BYTE 0DH, 0AH, 00H

HLPTXT          .BYTE 0DH, 0AH
                .TEXT "R           - Reset"
                .BYTE 0DH, 0AH
                .TEXT "BC or BW    - ROM BASIC Cold or Warm"
                .BYTE 0DH, 0AH
                .TEXT "X           - Boot CP/M (load $D000-$FFFF from disk)"
                .BYTE 0DH, 0AH
                .TEXT ":nnnnnn...  - Load Intel-Hex file record"
                .BYTE 0DH, 0AH
                .BYTE 00H

;------------------------------------------------------------------------------

                .ORG  4000H

PRIMARYIO       .DS   1
SECNO           .DS   1
DMAADDR         .DS   2

STACKSPACE      .DS   32
STACK           .EQU  $                     ; Stack top

                .END
