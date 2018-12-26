;
; Hello World .COM for CP/M environment
;
; Upload as CP/M binary then type HELLOCPM to run.
;

; ---------------------------------------------------------------------------
; Constant definitions

BDOS            .EQU  05H
STROUT          .EQU  09H

; ---------------------------------------------------------------------------
; Compilation address, standard CP/M entry point

                .ORG  0100H

; ---------------------------------------------------------------------------
; Program code entry point

EXECUTE         LD    DE, HELLOWORLD
                LD    C, STROUT
                CALL  BDOS
                RET

; ---------------------------------------------------------------------------
; Data

HELLOWORLD      .DB   "Hello world!", 13, 10, "$"

                .END
