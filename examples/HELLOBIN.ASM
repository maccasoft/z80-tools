;
; Hello World .BIN for MONITOR environment
;
; Upload as Intel HEX then type G5000 to run.
;

; ---------------------------------------------------------------------------
; Constant definitions

CHPUT           .EQU  08H

; ---------------------------------------------------------------------------
; Compilation address

                .ORG  5000H

; ---------------------------------------------------------------------------
; Program code entry point

EXECUTE
                LD    HL, HELLOWORLD
LOOP            LD    A, (HL)
                AND   A
                RET   Z
                CALL  CHPUT
                INC   HL
                JR    LOOP

; ---------------------------------------------------------------------------
; Data

HELLOWORLD      .DB   "Hello world!", 13, 10, 0

                .END
