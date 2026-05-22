package com.deskpet

enum class PetState {
    IDLE, THINKING, BUILDING, WORKING, HAPPY, SLEEPING, ERROR, DONE
}

class PetEngine {

    private var currentState: PetState = PetState.IDLE
    private var stateSince: Long = System.currentTimeMillis() / 1000

    var onStateChange: ((PetState) -> Unit)? = null

    fun getState(): PetState = currentState

    fun getStateData(): Map<String, Any> = mapOf(
        "state" to currentState.name.lowercase(),
        "since" to stateSince
    )

    fun setState(newState: PetState) {
        if (currentState == newState) return
        currentState = newState
        stateSince = System.currentTimeMillis() / 1000
        onStateChange?.invoke(newState)
    }

    fun transition(command: String) {
        when (command) {
            "thinking" -> setState(PetState.THINKING)
            "building" -> setState(PetState.BUILDING)
            "working" -> setState(PetState.WORKING)
            "done" -> {
                setState(PetState.HAPPY)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    if (currentState == PetState.HAPPY) setState(PetState.IDLE)
                }, 5000)
            }
            "error" -> setState(PetState.ERROR)
            "sleeping" -> setState(PetState.SLEEPING)
            "idle" -> setState(PetState.IDLE)
            "attention" -> {
                if (currentState == PetState.SLEEPING) {
                    setState(PetState.IDLE)
                } else {
                    setState(PetState.HAPPY)
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (currentState == PetState.HAPPY) setState(PetState.IDLE)
                    }, 3000)
                }
            }
        }
    }
}