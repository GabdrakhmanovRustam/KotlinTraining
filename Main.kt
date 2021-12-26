package minesweeper

const val COUNT_CELLS_X: Int = 9
const val COUNT_CELLS_Y: Int = 9


class Cell {
    var state: CellState = CellState.EMPTY
    var isHidden: Boolean = true
    var isMarked: Boolean = false

    fun markCell() {
        if (isHidden) {
            this.isMarked = !isMarked
        }
    }

    fun openCell() {
        if (isHidden) {
            this.isHidden = false
            this.isMarked = false
        }
    }

    fun detonateMine() {
        this.state = CellState.DETONATED
        this.isHidden = false
    }
}

enum class CellState {
    EMPTY, /* There is no mine in the Cell */
    MINE, /* There is a mine in the Cell, but it is not detonated */
    NUMBER, /* There is a number */
    DETONATED; /* There is a mine in the Cell, and it is blown up*/
}

object Minefield {
    val minefield = MutableList(COUNT_CELLS_X) { MutableList(COUNT_CELLS_Y) { Cell() } }
    private val minesNear = MutableList(COUNT_CELLS_X) { MutableList(COUNT_CELLS_Y) { 0 } }


    fun minefield(numMines: Int) {
        var i = 0
        while (i < numMines) {
            i++
            val x = (0 until COUNT_CELLS_X).random()
            val y = (0 until COUNT_CELLS_Y).random()
            if (minefield[x][y].state != CellState.MINE) {
                minefield[x][y].state = CellState.MINE
                for (i in -1..1) {
                    for (j in -1..1) {
                        if (x + i in 0 until COUNT_CELLS_X && y + j in 0 until COUNT_CELLS_Y && minefield[x + i][y + j].state != CellState.MINE) {
                            minefield[x + i][y + j].state = CellState.NUMBER
                            minesNear[x + i][y + j]++
                        }
                    }
                }
            } else {
                i--
            }
        }
    }

    fun floodFill(line: Int, row: Int) {
        val list = mutableListOf(mutableListOf<Int>(line, row))
        while (list.isNotEmpty()) {
            val x = list[0][0]
            val y = list[0][1]
            list.removeAt(0)
            if (minefield[x][y].state == CellState.EMPTY) {
                minefield[x][y].openCell()
                for (i in -1..1) {
                    for (j in -1..1) {
                        if (x + i in 0 until COUNT_CELLS_X && y + j in 0 until COUNT_CELLS_Y && minefield[x + i][y + j].isHidden) {
                            list.add(mutableListOf(x + i, y + j))
                        }
                    }
                }
            } else if (minefield[x][y].state == CellState.NUMBER) {
                minefield[x][y].openCell()
            }
        }
    }

    // detonate all mines
    fun detonateAll() {
        for (line in minefield) {
            for (cell in line) {
                if (cell.state == CellState.MINE) {
                    cell.detonateMine()
                }
            }
        }
    }

    // Output minefield
    fun outputMinefield() {
        println("-|123456789|")
        println("-|---------|")
        for (i in 0 until COUNT_CELLS_Y) {
            print("${i + 1}|")
            for (j in 0 until COUNT_CELLS_X) {
                if (minefield[j][i].isMarked) {
                    print("*")
                } else if (minefield[j][i].isHidden) {
                    print(".")
                } else {
                    when (this.minefield[j][i].state) {
                        CellState.DETONATED -> print("X")
                        CellState.NUMBER -> print(minesNear[j][i])
                        else -> {
                            print("/")
                        }
                    }
                }
            }
            println("|")
        }
        println("-|---------|")
    }
}

fun main() {
    println("How many mines do you want on the field?")
    val numMines = readLine()!!.toInt()
    Minefield.minefield(numMines)
    Minefield.outputMinefield()
    var detonate = false

    var markedCells = 0
    var markedMines = 0
    while (numMines != markedCells && numMines != markedMines && !detonate) {
        print("Set/unset mine marks or claim a cell as free:")
        val (a, b, command) = readLine()!!.split(" ")
        val line = a.toInt() - 1
        val row = b.toInt() - 1

        when (command) {
            "mine" -> {
                Minefield.minefield[line][row].markCell()
                if (Minefield.minefield[line][row].state == CellState.MINE && !Minefield.minefield[line][row].isMarked) {
                    markedCells++
                    markedMines++
                } else if (Minefield.minefield[line][row].state == CellState.MINE && Minefield.minefield[line][row].isMarked) {
                    markedCells--
                    markedMines--
                } else if (Minefield.minefield[line][row].state != CellState.MINE && !Minefield.minefield[line][row].isMarked) {
                    markedCells++
                } else if (Minefield.minefield[line][row].state != CellState.MINE && Minefield.minefield[line][row].isMarked) {
                    markedCells--

                }
            }
            "free" -> {
                when (Minefield.minefield[line][row].state) {
                    CellState.MINE -> {
                        Minefield.detonateAll()
                        detonate = true
                    }
                    else -> {
                        Minefield.floodFill(line, row)
                    }
                }
            }
        }
        Minefield.outputMinefield()
    }
    if (detonate) {
        println("You stepped on a mine and failed!")
    } else {
        println("Congratulations! You found all the mines!")
    }
}
