package adventofcode.y2020.day8

import java.lang.Exception
import java.util.BitSet
import kotlin.system.exitProcess

enum class InstructionType {
    NOP,
    ACC,
    JMP,
    TER
}

data class Instruction(
        val type: InstructionType,
        val count: Int
)

data class Handheld(
        val instructions: Array<Instruction>,
        val executionPoint: Int = 0,
        val accumulator: Int = 0,
        val exited: Boolean = false
) {
    fun executeOne(): Handheld {
        val curr = instructions[executionPoint]
        return when (curr.type) {
            InstructionType.NOP -> this.copy(executionPoint = executionPoint + 1)
            InstructionType.ACC -> this.copy(executionPoint = executionPoint + 1, accumulator = accumulator + curr.count)
            InstructionType.JMP -> this.copy(executionPoint = executionPoint + curr.count)
            InstructionType.TER -> this.copy(exited = true)
        }.also {
//            println("Before: $executionPoint $accumulator")
//            println("executing: $curr")
//            println("After: ${it.executionPoint} ${it.accumulator}")
        }
    }
}

fun runOneTracing(visitedInstructions: BitSet, handheld: Handheld): Pair<BitSet, Handheld> {
    val handheldAfter = handheld.executeOne()
    return if (handheldAfter.exited || visitedInstructions[handheldAfter.executionPoint]) {
        visitedInstructions to handheldAfter
    } else {
        visitedInstructions[handheldAfter.executionPoint] = true
        runOneTracing(visitedInstructions, handheldAfter)
    }
}

fun runUntilRepeat(handheld: Handheld): Handheld {
    val visitedInstructions = BitSet(handheld.instructions.size)
    return runOneTracing(visitedInstructions, handheld).second
}

fun doesItWork(instructions: Array<Instruction>): Handheld? =
    try {
        println(instructions.toList())
        val handheld = runUntilRepeat(Handheld(instructions + listOf(Instruction(InstructionType.TER, 0))))
        println("received: ${handheld.exited} ${handheld.executionPoint} ${handheld.accumulator}")
        if(handheld.exited)
            handheld
        else
            null
    } catch (e: Exception) {
        println(e.message)
        null
    }


fun main(args: Array<String>) {
    val instructions = Handheld::class.java.classLoader.getResource("2020day8.input")
            .readText()
            .lines()
            .filter { it.isNotBlank() }
            .map {
                val args = it.split(" ")
                Instruction(
                        InstructionType.valueOf(args[0].toUpperCase()),
                        args[1].toInt()
                )
            }.toTypedArray()
    val final = runUntilRepeat(Handheld(instructions))
    println("Final value: ${final.accumulator} at ${final.executionPoint}")

    for(index in instructions.indices) {
        println("trial with: $index")
        val handheld = when(instructions[index].type) {
            InstructionType.JMP -> {
                val testInstructions = instructions.clone().apply {
                    this[index] = this[index].copy(type = InstructionType.NOP)
                }
                doesItWork(testInstructions)
            }
            InstructionType.NOP -> {
                val testInstructions = instructions.clone().apply {
                    this[index] = this[index].copy(type = InstructionType.JMP)
                }
                doesItWork(testInstructions)
            }
            else -> null
        }
        if(handheld != null) {
            println("Finally $index ${handheld.accumulator} ${handheld.executionPoint}")
            return
        }
    }
    println("didn't find anything")
    exitProcess(-1)
}